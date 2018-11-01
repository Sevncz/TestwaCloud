package com.testwa.distest.server.web.device.controller;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.AuthorizedException;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.vo.ResultVO;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.DeviceShareScope;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.device.form.DeviceScopeNewForm;
import com.testwa.distest.server.service.device.form.DeviceScopeRemoveForm;
import com.testwa.distest.server.service.device.form.DeviceScopeShareToProjectForm;
import com.testwa.distest.server.service.device.form.DeviceScopeShareToUserForm;
import com.testwa.distest.server.service.device.service.DeviceShareScopeService;
import com.testwa.distest.server.service.device.service.DeviceSharerService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.validator.UserValidator;
import com.testwa.distest.server.web.device.validator.DeviceValidatoer;
import com.testwa.distest.server.web.device.vo.DeviceScopeUserVO;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import javax.validation.Valid;

import java.util.List;

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;

@Slf4j
@Api("设备权限配置相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/deviceScope")
public class DeviceScopeConfigController extends BaseController{

    @Autowired
    private DeviceShareScopeService deviceShareScopeService;
    @Autowired
    private DeviceSharerService deviceSharerService;
    @Autowired
    private DeviceValidatoer deviceValidatoer;
    @Autowired
    private UserService userService;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private ProjectValidator projectValidator;


    /**
     * 配置设备分享范围
     * @param form
     * @return
     */
    @ApiOperation(value="配置设备分享范围", notes = "只能管理自己的设备")
    @ResponseBody
    @PostMapping(value = "/config")
    public ResultVO config(@RequestBody @Valid DeviceScopeNewForm form) throws ObjectNotExistsException, AuthorizedException, AccountNotFoundException {
        Device device = deviceValidatoer.validateDeviceExist(form.getDeviceId());

        User user = userService.findByUsername(getCurrentUsername());
        // 设备当前登录用户和网站登录用户一致
        if(!user.getId().equals(device.getLastUserId())) {
            return fail(ResultCode.ILLEGAL_OP, "该设备未连接您的客户端");
        }

        deviceShareScopeService.updateOrSave(form.getDeviceId(), user.getId(), form.getScope());

        return ok();
    }

    /**
     * 配置设备分享的用户
     * @param form
     * @return
     */
    @ApiOperation(value="配置设备分享的用户", notes = "只能管理自己的设备")
    @ResponseBody
    @PostMapping(value = "/shareTo/user")
    public ResultVO shareToUser(@RequestBody @Valid DeviceScopeShareToUserForm form) throws ObjectNotExistsException, AuthorizedException, AccountNotFoundException {
        Device device = deviceValidatoer.validateDeviceExist(form.getDeviceId());
        if(form.getToUserIdList() == null || form.getToUserIdList().isEmpty()) {
            return fail(ResultCode.PARAM_ERROR, "指定分享的用户列表不能为空");
        }
        userValidator.validateUserIdsExist(form.getToUserIdList());

        User user = userService.findByUsername(getCurrentUsername());
        // 设备当前登录用户和网站登录用户一致
        if(!user.getId().equals(device.getLastUserId())) {
            return fail(ResultCode.ILLEGAL_OP, "该设备未连接您的客户端");
        }
        DeviceShareScope scope = deviceShareScopeService.findOneByDeviceIdAndCreateBy(form.getDeviceId(), user.getId());
        if(DB.DeviceShareScopeEnum.Protected.equals(scope.getShareScope())) {
            deviceSharerService.insertList(form.getDeviceId(), user.getId(), form.getToUserIdList());
        }else{
            return fail(ResultCode.ILLEGAL_OP, "您无法分享给指定用户");
        }

        return ok();
    }

    /**
     * 移除设备分享的用户
     * @param form
     * @return
     */
    @ApiOperation(value="移除设备分享的用户", notes = "只能管理自己的设备")
    @ResponseBody
    @PostMapping(value = "/remove/user")
    public ResultVO removeUser(@RequestBody @Valid DeviceScopeRemoveForm form) {
        if(form.getScopeIdList() == null || form.getScopeIdList().isEmpty()) {
            return fail(ResultCode.PARAM_ERROR, "参数不能为空");
        }

        deviceSharerService.removeList(form.getScopeIdList());

        return ok();
    }

    /**
     * 设备分享的用户列表
     * @param deviceId
     * @return
     */
    @ApiOperation(value="设备分享的用户列表", notes = "只能管理自己的设备")
    @ResponseBody
    @GetMapping(value = "/{deviceId}/share/userlist")
    public ResultVO shareList(@PathVariable String deviceId) {

        User user = userService.findByUsername(getCurrentUsername());

        List<DeviceScopeUserVO> vos = deviceSharerService.findDeviceScopeUserList(deviceId, user.getId());

        return ok(vos);
    }

    /**
     * 配置设备分享的项目
     * @param form
     * @return
     */
    @ApiOperation(value="配置设备分享的项目", notes = "只能管理自己的设备")
    @ResponseBody
    @PostMapping(value = "/shareTo/project")
    public ResultVO shareToProject(@RequestBody @Valid DeviceScopeShareToProjectForm form) throws ObjectNotExistsException, AuthorizedException, AccountNotFoundException {
        deviceValidatoer.validateDeviceExist(form.getDeviceId());
        if(form.getToProjectIdList() == null || form.getToProjectIdList().isEmpty()) {
            return fail(ResultCode.PARAM_ERROR, "指定分享的项目列表不能为空");
        }
        projectValidator.validateProjectExist(form.getToProjectIdList());

        User user = userService.findByUsername(getCurrentUsername());

        return ok();
    }

}
