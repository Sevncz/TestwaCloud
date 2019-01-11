package com.testwa.distest.server.web.device.controller;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.exception.BusinessException;
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
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
@Api("设备权限配置相关api")
@Validated
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
    @Autowired
    private User currentUser;

    /**
     * 配置设备分享范围
     * @param form
     * @return
     */
    @ApiOperation(value="配置设备分享范围", notes = "只能管理自己的设备")
    @ResponseBody
    @PostMapping(value = "/config")
    public Result config(@RequestBody @Valid DeviceScopeNewForm form) {
        Device device = deviceValidatoer.validateDeviceExist(form.getDeviceId());

        // 设备当前登录用户和网站登录用户一致
        if(!currentUser.getId().equals(device.getLastUserId())) {
            return Result.error(ResultCode.ILLEGAL_OP, "该设备未连接您的客户端");
        }

        deviceShareScopeService.updateOrSave(form.getDeviceId(), currentUser.getId(), form.getScope());

        return Result.success();
    }

    /**
     * 配置设备分享的用户
     * @param form
     * @return
     */
    @ApiOperation(value="配置设备分享的用户", notes = "只能管理自己的设备")
    @ResponseBody
    @PostMapping(value = "/shareToUser")
    public Result shareToUser(@RequestBody @Valid DeviceScopeShareToUserForm form) {
        Device device = deviceValidatoer.validateDeviceExist(form.getDeviceId());
        if(form.getAddUserId() != null && !form.getAddUserId().isEmpty()) {
            userValidator.validateUserIdsExist(new ArrayList<>(form.getAddUserId()));
        }
        if(form.getRemoveUserId() != null && !form.getRemoveUserId().isEmpty()) {
            userValidator.validateUserIdsExist(new ArrayList<>(form.getRemoveUserId()));
        }
        if(!form.getAddUserId().isEmpty() && !form.getRemoveUserId().isEmpty()) {
            Set<Long> retainResult = new HashSet<>(form.getAddUserId());
            retainResult.retainAll(form.getRemoveUserId());
            if(!retainResult.isEmpty()) {
                return Result.error(ResultCode.ILLEGAL_OP, "一个用户不能同时被添加和删除");
            }
        }
        // 设备当前登录用户和网站登录用户一致
        if(!currentUser.getId().equals(device.getLastUserId())) {
            return Result.error(ResultCode.ILLEGAL_OP, "该设备未连接您的客户端");
        }

        if(form.getAddUserId().contains(currentUser.getId()) || form.getRemoveUserId().contains(currentUser.getId())) {
            return Result.error(ResultCode.ILLEGAL_OP, "不能添加和删除自己");
        }

        DeviceShareScope scope = deviceShareScopeService.findOneByDeviceIdAndCreateBy(form.getDeviceId(), currentUser.getId());
        if(DB.DeviceShareScopeEnum.Protected.equals(scope.getShareScope())) {
            deviceSharerService.insertList(form.getDeviceId(), currentUser.getId(), form.getAddUserId());
            deviceSharerService.removeList(form.getDeviceId(), currentUser.getId(), form.getRemoveUserId());
        }else{
            return Result.error(ResultCode.ILLEGAL_OP, "您无法分享给指定用户");
        }

        return Result.success();
    }

    /**
     * 移除设备分享的用户
     * @param form
     * @return
     */
    @ApiOperation(value="移除设备分享的用户", notes = "只能管理自己的设备")
    @ResponseBody
    @PostMapping(value = "/removeUser")
    public Result removeUser(@RequestBody @Valid DeviceScopeRemoveForm form) {
        Device device = deviceValidatoer.validateDeviceExist(form.getDeviceId());
        // 设备当前登录用户和网站登录用户一致
        if(!currentUser.getId().equals(device.getLastUserId())) {
            return Result.error(ResultCode.ILLEGAL_OP, "该设备未连接您的客户端");
        }

        deviceSharerService.removeOne(form.getDeviceId(), form.getUserId(), currentUser.getId());

        return Result.success();
    }

    /**
     * 设备分享的用户列表
     * @param deviceId
     * @return
     */
    @ApiOperation(value="设备分享的用户列表", notes = "只能管理自己的设备")
    @ResponseBody
    @GetMapping(value = "/{deviceId}/userList")
    public List userList(@PathVariable String deviceId) {
        return deviceSharerService.listDeviceScopeUser(deviceId, currentUser.getId());
    }

    /**
     * 配置设备分享的项目
     * @param form
     * @return
     */
    @ApiOperation(value="配置设备分享的项目", notes = "只能管理自己的设备")
    @ResponseBody
    @PostMapping(value = "/shareToProject")
    public void shareToProject(@RequestBody @Valid DeviceScopeShareToProjectForm form) {
        deviceValidatoer.validateDeviceExist(form.getDeviceId());
        if(form.getToProjectIdList() == null || form.getToProjectIdList().isEmpty()) {
            throw new BusinessException(ResultCode.ILLEGAL_PARAM, "指定分享的项目列表不能为空");
        }
        projectValidator.validateProjectExist(form.getToProjectIdList());
        // TODO
    }

}
