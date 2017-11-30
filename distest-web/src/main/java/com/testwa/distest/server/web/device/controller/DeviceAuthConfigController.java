package com.testwa.distest.server.web.device.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.AuthorizedException;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.vo.PageResult;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.device.form.DeviceAuthNewForm;
import com.testwa.distest.server.service.device.form.DeviceAuthRemoveForm;
import com.testwa.distest.server.service.device.form.DeviceListForm;
import com.testwa.distest.server.service.device.service.DeviceAuthService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.validator.UserValidator;
import com.testwa.distest.server.web.device.validator.DeviceValidatoer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import javax.validation.Valid;
import java.util.Set;

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;

@Log4j2
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
    public Result save(@RequestBody @Valid DeviceAuthNewForm form) throws ObjectNotExistsException, AuthorizedException, AccountNotFoundException {
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
    public Result remove(@RequestBody @Valid DeviceAuthRemoveForm form) throws ObjectNotExistsException, AuthorizedException, AccountNotFoundException {
        deviceValidatoer.validateOnline(form.getDeviceId());
        deviceValidatoer.validateDeviceExist(form.getDeviceId());
        userValidator.validateUserIdsExist(form.getUserIds());
        User user = userService.findByUsername(getCurrentUsername());
        deviceAuthService.removeSomeFromDevice(form, user.getId());
        return ok();
    }

}
