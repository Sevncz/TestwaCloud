package com.testwa.distest.server.web.device.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.AccountException;
import com.testwa.core.base.exception.AuthorizedException;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.vo.PageResult;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.device.form.DeviceAuthNewForm;
import com.testwa.distest.server.service.device.form.DeviceBatchCheckForm;
import com.testwa.distest.server.service.device.form.DeviceListForm;
import com.testwa.distest.server.service.device.service.DeviceAuthService;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.device.auth.DeviceAuthMgr;
import com.testwa.distest.server.web.device.validator.DeviceValidatoer;
import com.testwa.distest.server.web.device.vo.CheckDeviceResultVO;
import com.testwa.distest.server.web.device.vo.DeviceCategoryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;

/**
 * Created by wen on 7/30/16.
 */
@Slf4j
@Api("设备相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/device")
public class DeviceController extends BaseController {
    @Autowired
    private UserService userService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceAuthService deviceAuthService;
    @Autowired
    private DeviceAuthMgr deviceAuthMgr;
    @Autowired
    private DeviceValidatoer deviceValidatoer;


    /**
     * 可见在线设备分页列表
     * @param form
     * @return
     */
    @ApiOperation(value="查看用户可见的在线设备分页列表")
    @ApiImplicitParam(name = "form", value = "分页查询列表", required = true, dataType = "DeviceListForm")
    @ResponseBody
    @GetMapping(value = "/enable/page")
    public Result enablePage(@Valid DeviceListForm form) throws ObjectNotExistsException, AuthorizedException {
        Set<String> deviceIds = deviceAuthMgr.allOnlineDevices();
        if(deviceIds.size() == 0 ){
            return ok(new PageResult<>(Arrays.asList(), 0));
        }
        PageResult<Device> devicePR = deviceService.findOnlinePage(deviceIds, form);
        return ok(devicePR);
    }

    /**
     * 可见在线设备列表
     * @param form
     * @return
     */
    @ApiOperation(value="查看用户可见的在线设备列表", notes = "设备目前所有人均可见")
    @ResponseBody
    @GetMapping(value = "/enable/list")
    public Result enableList(@Valid DeviceListForm form) throws AccountException, ObjectNotExistsException, AuthorizedException {
        Set<String> deviceIds = deviceAuthMgr.allEnableDevices();
        if(deviceIds.size() == 0 ){
            return ok(Arrays.asList());
        }
        List<Device> deviceList = deviceService.findOnlineList(deviceIds, form);
        return ok(deviceList);
    }

    /**
     * 所有设备分页列表
     * @param form
     * @return
     */
    @ApiOperation(value="查看所有设备分页列表")
    @ResponseBody
    @GetMapping(value = "/all/page")
    public Result allPage(@Valid DeviceListForm form) throws ObjectNotExistsException, AuthorizedException {
        Set<String> deviceIds = deviceAuthMgr.allEnableDevices();
        PageResult<Device> devicePR = deviceService.findByPage(deviceIds, form);
        return ok(devicePR);
    }
    /**
     * 所有设备列表
     * @param form
     * @return
     */
    @ApiOperation(value="所有设备列表")
    @ResponseBody
    @GetMapping(value = "/all/list")
    public Result allList(@Valid DeviceListForm form) throws ObjectNotExistsException, AuthorizedException {
        Set<String> deviceIds = deviceAuthMgr.allEnableDevices();
        List<Device> devices = deviceService.findList(deviceIds, form);
        return ok(devices);
    }

    @ApiOperation(value="查看登录用户自己的在线设备，获得包括设备权限和用户信息")
    @ResponseBody
    @GetMapping(value = "/my/list")
    public Result myList() {
        Set<String> deviceIds = deviceAuthMgr.allEnableDevices();
        User user = userService.findByUsername(getCurrentUsername());
        List<Device> deviceList = deviceService.fetchList(deviceIds, user.getId());
        return ok(deviceList);
    }


    @ApiOperation(value="把自己的设备分享给其他人", notes = "暂时所有设备都可见，可不使用")
    @ResponseBody
    @PostMapping(value = "/share/to/other")
    public Result shareToOther(@RequestBody DeviceAuthNewForm form) throws ObjectNotExistsException {
        //  校验设备是否在线
        deviceValidatoer.validateOnline(form.getDeviceId());
        User currentUser = userService.findByUsername(getCurrentUsername());
        //  校验设备是否是用户自己的设备
        deviceValidatoer.validateDeviceBelongUser(form.getDeviceId(), currentUser.getId());
        //  保存到数据库
        deviceAuthService.insert(form, currentUser.getId());
        return ok();
    }

    @RequestMapping(value = "/screen", method = RequestMethod.GET)
    public Result getScreen() {
        return ok();
    }


    @ApiOperation(value="Android设备的分类，各个维度", notes = "")
    @ResponseBody
    @GetMapping(value = "/category/android")
    public Result category() throws ObjectNotExistsException {
        Set<String> deviceIds = deviceAuthMgr.allEnableDevices();
        DeviceCategoryVO vo = deviceService.getCategory(deviceIds);
        return ok(vo);
    }

    @ApiOperation(value="检查设备是否可用", notes = "")
    @ResponseBody
    @PostMapping(value = "/check/usable")
    public Result checkUsable(@RequestBody DeviceBatchCheckForm form) throws ObjectNotExistsException {
        if(form.getDeviceIds() == null || form.getDeviceIds().size() == 0) {
            return ok();
        }
        List<String> deviceIds = form.getDeviceIds();
        CheckDeviceResultVO vo = new CheckDeviceResultVO();
        for(String deviceId: deviceIds) {
            // 检查是否在线
            deviceValidatoer.validateOnline(deviceId);
            // 检查是否在工作中
            Device device = deviceService.findByDeviceId(deviceId);
            if (!DB.DeviceWorkStatus.FREE.equals(device.getWorkStatus()) || !DB.DeviceDebugStatus.FREE.equals(device.getDebugStatus())) {
                vo.setStatus(false);
                vo.addUnusableDevice(device);
            }
        }

        return ok(vo);
    }


}
