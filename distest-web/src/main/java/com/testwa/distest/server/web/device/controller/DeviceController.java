package com.testwa.distest.server.web.device.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.*;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.service.cache.mgr.DeviceLockCache;
import com.testwa.distest.server.service.device.form.DeviceBatchCheckForm;
import com.testwa.distest.server.service.device.form.DeviceSearchForm;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.device.auth.DeviceAuthMgr;
import com.testwa.distest.server.web.device.validator.DeviceValidatoer;
import com.testwa.distest.server.web.device.vo.CheckDeviceResultVO;
import com.testwa.distest.server.web.device.vo.DeviceCategoryVO;
import com.testwa.distest.server.web.device.vo.DeviceLockResultVO;
import com.testwa.distest.server.web.device.vo.DeviceUnLockResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

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
    private DeviceAuthMgr deviceAuthMgr;
    @Autowired
    private DeviceValidatoer deviceValidatoer;
    @Autowired
    private DeviceLockCache deviceLockMgr;
    @Value("${lock.debug.expire}")
    private Integer debugExpireTime;

    /**
     * 云端设备列表
     * @param form
     * @return
     */
    @ApiOperation(value="云端设备列表")
    @ResponseBody
    @GetMapping(value = "/cloud/list")
    public Result cloudList(@Valid DeviceSearchForm form) throws ObjectNotExistsException, AuthorizedException {
        Set<String> deviceIds = deviceAuthMgr.allOnlineDevices();
        List<Device> devices = deviceService.findCloudList(deviceIds, form.getBrand(), form.getOsVersion(), form.getResolution(), form.getIsAll());
        return ok(devices);
    }

    /**
     * 云端设备模糊查询列表
     * @param form
     * @return
     */
    @ApiOperation(value="云端设备模糊查询列表")
    @ResponseBody
    @GetMapping(value = "/cloud/search")
    public Result enableSearch(@Valid DeviceSearchForm form) throws ObjectNotExistsException, AuthorizedException {
        Set<String> deviceIds = deviceAuthMgr.allOnlineDevices();
        List<Device> devices = deviceService. searchCloudList(deviceIds, form.getBrand(), form.getOsVersion(), form.getResolution(), form.getIsAll());
        return ok(devices);
    }

    @ApiOperation(value="在线Android设备的分类，各个维度", notes = "")
    @ResponseBody
    @GetMapping(value = "/category/android")
    public Result category() throws ObjectNotExistsException {
        Set<String> deviceIds = deviceAuthMgr.allOnlineDevices();
        DeviceCategoryVO vo = new DeviceCategoryVO();
        if(deviceIds != null && deviceIds.size() > 0) {
            vo = deviceService.getCategory(deviceIds);
            List<Device> deviceList =  deviceService.findOnlineList(deviceIds);
            vo.setDeviceNum(deviceList.size());
        }
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
            boolean islocked = deviceLockMgr.isLocked(deviceId);
            if(islocked) {
                Device unableDev = deviceService.findByDeviceId(deviceId);
                vo.setStatus(false);
                vo.addUnusableDevice(unableDev);
            }else{
                try {
                    deviceValidatoer.validateUsable(deviceId);
                }catch (DeviceUnusableException | ObjectNotExistsException e) {
                    log.error("设备忙碌中 {}", deviceId, e);
                    Device unableDev = deviceService.findByDeviceId(deviceId);
                    vo.setStatus(false);
                    vo.addUnusableDevice(unableDev);
                }
            }
        }
        return ok(vo);
    }

    @ApiOperation(value="锁定设备", notes = "")
    @ResponseBody
    @PostMapping(value = "/lock/debug/{deviceId}")
    public Result lockDebug(@PathVariable String deviceId) throws ObjectNotExistsException {
        if(StringUtils.isBlank(deviceId)) {
            throw new ParamsIsNullException("设备ID不能为空");
        }
//        deviceValidatoer.validateUsable(deviceId);
//
//        String username = WebUtil.getCurrentUsername();
//        User user = userService.findByUsername(username);
//        boolean islock = deviceLockMgr.lock(deviceId, user.getUserCode(), debugExpireTime);
//
//        deviceService.debugging(deviceId);

        DeviceLockResultVO vo = new DeviceLockResultVO();
        vo.setSuccess(true);
        return ok(vo);
    }

    @ApiOperation(value="选择的时候锁定设备", notes = "")
    @ResponseBody
    @PostMapping(value = "/lock/select/{deviceId}")
    public Result lockSelect(@PathVariable String deviceId) throws ObjectNotExistsException {
        if(StringUtils.isBlank(deviceId)) {
            throw new ParamsIsNullException("设备ID不能为空");
        }
//        deviceValidatoer.validateUsable(deviceId);
//
//        int selectTime = 120; // 2分钟选择设备的时间
//        String username = WebUtil.getCurrentUsername();
//        User user = userService.findByUsername(username);
//        boolean islock = deviceLockMgr.lock(deviceId, user.getUserCode(), selectTime);
        DeviceLockResultVO vo = new DeviceLockResultVO();
        vo.setSuccess(true);
        return ok(vo);
    }

    @ApiOperation(value="解除锁定设备", notes = "")
    @ResponseBody
    @PostMapping(value = "/unlock/{deviceId}")
    public Result unlock(@PathVariable String deviceId) throws ObjectNotExistsException {
        if(StringUtils.isBlank(deviceId)) {
            throw new ParamsIsNullException("设备ID不能为空");
        }
//        String username = WebUtil.getCurrentUsername();
//        User user = userService.findByUsername(username);
//        boolean isSuccess = deviceLockMgr.release(deviceId, user.getUserCode());
        DeviceUnLockResultVO vo = new DeviceUnLockResultVO();
//        vo.setSuccess(isSuccess);
//        if(!isSuccess) {
//            vo.setError("用户不匹配，无法解锁");
//        }else {
//            deviceService.debugFree(deviceId);
//            vo.setError("解锁成功");
//        }
        deviceService.debugFree(deviceId);
        vo.setError("解锁成功");
        return ok(vo);
    }

}
