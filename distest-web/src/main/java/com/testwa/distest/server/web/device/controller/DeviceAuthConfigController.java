package com.testwa.distest.server.web.device.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.AuthorizedException;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.vo.ResultVO;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.device.form.DeviceAuthNewForm;
import com.testwa.distest.server.service.device.form.DeviceAuthRemoveForm;
import com.testwa.distest.server.service.device.service.DeviceAuthService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.validator.UserValidator;
import com.testwa.distest.server.web.device.validator.DeviceValidatoer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import javax.validation.Valid;

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;

@Slf4j
@Api("设备权限配置相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/deviceAuth")
public class DeviceAuthConfigController extends BaseController{

    @Autowired
    private DeviceAuthService deviceAuthService;
    @Autowired
    private DeviceValidatoer deviceValidatoer;
    @Autowired
    private UserService userService;
    @Autowired
    private UserValidator userValidator;


    /**
     * 添加设备的可用用户
     * @param form
     * @return
     */
    @ApiOperation(value="配置设备的可用用户", notes = "只能管理自己的设备")
    @ResponseBody
    @RequestMapping(value = "/save", method = RequestMethod.GET)
    public ResultVO save(@RequestBody @Valid DeviceAuthNewForm form) throws ObjectNotExistsException, AuthorizedException, AccountNotFoundException {
        deviceValidatoer.validateOnline(form.getDeviceId());
        deviceValidatoer.validateDeviceExist(form.getDeviceId());
        userValidator.validateUserIdsExist(form.getUserIds());
        User user = userService.findByUsername(getCurrentUsername());
        deviceAuthService.insert(form, user.getId());
        return ok();
    }

    /**
     * 移除设备的可用用户
     * @param form
     * @return
     */
    @ApiOperation(value="配置设备的可用用户", notes = "只能管理自己的设备")
    @ResponseBody
    @RequestMapping(value = "/remove", method = RequestMethod.GET)
    public ResultVO remove(@RequestBody @Valid DeviceAuthRemoveForm form) throws ObjectNotExistsException, AuthorizedException, AccountNotFoundException {
        deviceValidatoer.validateOnline(form.getDeviceId());
        deviceValidatoer.validateDeviceExist(form.getDeviceId());
        userValidator.validateUserIdsExist(form.getUserIds());
        User user = userService.findByUsername(getCurrentUsername());
        deviceAuthService.removeSomeFromDevice(form, user.getId());
        return ok();
    }

}
