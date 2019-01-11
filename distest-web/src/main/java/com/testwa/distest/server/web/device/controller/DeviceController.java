package com.testwa.distest.server.web.device.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.exception.DeviceException;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.cache.mgr.DeviceLockCache;
import com.testwa.distest.server.service.device.dto.PrivateDeviceDTO;
import com.testwa.distest.server.service.device.form.DeviceBatchCheckForm;
import com.testwa.distest.server.service.device.form.DeviceSearchForm;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.web.device.mgr.DeviceOnlineMgr;
import com.testwa.distest.server.web.device.validator.DeviceValidatoer;
import com.testwa.distest.server.web.device.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;


/**
 * Created by wen on 7/30/16.
 */
@Slf4j
@Api("设备相关api")
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/device")
public class DeviceController extends BaseController {
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceOnlineMgr deviceOnlineMgr;
    @Autowired
    private DeviceValidatoer deviceValidatoer;
    @Autowired
    private DeviceLockCache deviceLockMgr;
    @Value("${lock.debug.expire}")
    private Integer debugExpireTime;
    @Autowired
    private User currentUser;

    /**
     * 云端设备列表
     * @param form
     * @return
     */
    @ApiOperation(value="云端设备列表")
    @ResponseBody
    @GetMapping(value = "/cloud/list")
    public List<Device> cloudList(@Valid DeviceSearchForm form) {
        Set<String> deviceIds = deviceOnlineMgr.allOnlineDevices();
        return deviceService.findCloudList(deviceIds, form.getBrand(), form.getOsVersion(), form.getResolution(), form.getIsAll());
    }

    /**
     * 我的设备列表
     * @param form
     * @return
     */
    @ApiOperation(value="我的设备列表")
    @ResponseBody
    @GetMapping(value = "/private/list")
    public List<PrivateDeviceVO> privateList(@Valid DeviceSearchForm form) {
        Set<String> deviceIds = deviceOnlineMgr.allOnlineDevices();

        List<PrivateDeviceDTO> privateDeviceDTOList = deviceService.findPrivateList(deviceIds, currentUser.getId(), form.getBrand(), form.getOsVersion(), form.getResolution(), form.getIsAll());
        return buildVOs(privateDeviceDTOList, PrivateDeviceVO.class);
    }

    /**
     * 分享给我的设备列表
     * @param form
     * @return
     */
    @ApiOperation(value="分享给我的设备列表")
    @ResponseBody
    @GetMapping(value = "/share/list")
    public List<Device> shareList(@Valid DeviceSearchForm form) {
        Set<String> deviceIds = deviceOnlineMgr.allOnlineDevices();

        return deviceService.findShareToUserList(deviceIds, currentUser.getId(), form.getBrand(), form.getOsVersion(), form.getResolution(), form.getIsAll());
    }

    /**
     * 云端设备模糊查询列表
     * @param form
     * @return
     */
    @ApiOperation(value="云端设备模糊查询列表")
    @ResponseBody
    @GetMapping(value = "/cloud/search")
    public List<Device> cloudSearch(@Valid DeviceSearchForm form) {
        Set<String> deviceIds = deviceOnlineMgr.allOnlineDevices();
        return deviceService. searchCloudList(deviceIds, form.getBrand(), form.getOsVersion(), form.getResolution(), form.getIsAll());
    }

    /**
     * 个人设备模糊查询列表
     * @param form
     * @return
     */
    @ApiOperation(value="个人设备模糊查询列表")
    @ResponseBody
    @GetMapping(value = "/private/search")
    public List<PrivateDeviceVO> myEnableSearch(@Valid DeviceSearchForm form) {
        Set<String> deviceIds = deviceOnlineMgr.allOnlineDevices();
        List<PrivateDeviceDTO> privateDeviceDTOList = deviceService.searchPrivateList(deviceIds, currentUser.getId(), form.getBrand(), form.getOsVersion(), form.getResolution(), form.getIsAll());
        return buildVOs(privateDeviceDTOList, PrivateDeviceVO.class);
    }

    @ApiOperation(value="在线Android设备的分类，各个维度", notes = "")
    @ResponseBody
    @GetMapping(value = "/category/android")
    public DeviceCategoryVO category() {
        Set<String> deviceIds = deviceOnlineMgr.allOnlineDevices();
        DeviceCategoryVO vo = new DeviceCategoryVO();
        if(deviceIds != null && !deviceIds.isEmpty()) {
            vo = deviceService.getCategory(deviceIds);
            List<Device> deviceList =  deviceService.findOnlineList(deviceIds);
            vo.setDeviceNum(deviceList.size());
        }
        return vo;
    }

    @ApiOperation(value="检查设备是否可用", notes = "")
    @ResponseBody
    @PostMapping(value = "/check/usable")
    public CheckDeviceResultVO checkUsable(@RequestBody @Valid DeviceBatchCheckForm form) {
        if(form.getDeviceIds() == null || form.getDeviceIds().isEmpty()) {
            return null;
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
                }catch (DeviceException | BusinessException e) {
                    log.error("设备忙碌中 {}", deviceId, e);
                    Device unableDev = deviceService.findByDeviceId(deviceId);
                    vo.setStatus(false);
                    vo.addUnusableDevice(unableDev);
                }
            }
        }
        return vo;
    }

    @ApiOperation(value="锁定设备", notes = "")
    @ResponseBody
    @PostMapping(value = "/{deviceId}/lock")
    public DeviceLockResultVO lockDebug(@PathVariable String deviceId) {
        DeviceLockResultVO vo = new DeviceLockResultVO();
        vo.setSuccess(true);
        return vo;
    }

    @ApiOperation(value="解除锁定设备", notes = "")
    @ResponseBody
    @PostMapping(value = "/{deviceId}/unlock")
    public DeviceUnLockResultVO unlock(@PathVariable String deviceId) {
        DeviceUnLockResultVO vo = new DeviceUnLockResultVO();
        deviceService.debugFree(deviceId);
        vo.setError("解锁成功");
        return vo;
    }
}
