package com.testwa.distest.server.web.device.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.*;
import com.testwa.core.base.vo.ResultVO;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.cache.mgr.DeviceLockCache;
import com.testwa.distest.server.service.device.dto.PrivateDeviceDTO;
import com.testwa.distest.server.service.device.form.DeviceBatchCheckForm;
import com.testwa.distest.server.service.device.form.DeviceSearchForm;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.device.mgr.DeviceOnlineMgr;
import com.testwa.distest.server.web.device.validator.DeviceValidatoer;
import com.testwa.distest.server.web.device.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    private DeviceOnlineMgr deviceOnlineMgr;
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
    public ResultVO cloudList(@Valid DeviceSearchForm form) throws ObjectNotExistsException, AuthorizedException {
        Set<String> deviceIds = deviceOnlineMgr.allOnlineDevices();
        List<Device> devices = deviceService.findCloudList(deviceIds, form.getBrand(), form.getOsVersion(), form.getResolution(), form.getIsAll());
        return ok(devices);
    }

    /**
     * 我的设备列表
     * @param form
     * @return
     */
    @ApiOperation(value="我的设备列表")
    @ResponseBody
    @GetMapping(value = "/private/list")
    public ResultVO privateList(@Valid DeviceSearchForm form) throws ObjectNotExistsException, AuthorizedException {
        Set<String> deviceIds = deviceOnlineMgr.allOnlineDevices();

        User user = userService.findByUsername(getCurrentUsername());
        List<PrivateDeviceDTO> privateDeviceDTOList = deviceService.findPrivateList(deviceIds, user.getId(), form.getBrand(), form.getOsVersion(), form.getResolution(), form.getIsAll());
        // 枚举转换器，可根据需要添加
        ConvertUtils.register(new Converter() {
            @Override
            public Object convert(Class type, Object o) {
                return DB.PhoneOS.valueOf((Integer) o);
            }
        }, DB.PhoneOS.class);
        List<PrivateDeviceVO> result = buildVOs(privateDeviceDTOList, PrivateDeviceVO.class);

        return ok(result);
    }

    /**
     * 分享给我的设备列表
     * @param form
     * @return
     */
    @ApiOperation(value="分享给我的设备列表")
    @ResponseBody
    @GetMapping(value = "/share/list")
    public ResultVO shareList(@Valid DeviceSearchForm form) throws ObjectNotExistsException, AuthorizedException {
        Set<String> deviceIds = deviceOnlineMgr.allOnlineDevices();

        User user = userService.findByUsername(getCurrentUsername());

        List<Device> devices = deviceService.findShareToUserList(deviceIds, user.getId(), form.getBrand(), form.getOsVersion(), form.getResolution(), form.getIsAll());
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
    public ResultVO cloudSearch(@Valid DeviceSearchForm form) throws ObjectNotExistsException, AuthorizedException {
        Set<String> deviceIds = deviceOnlineMgr.allOnlineDevices();
        List<Device> devices = deviceService. searchCloudList(deviceIds, form.getBrand(), form.getOsVersion(), form.getResolution(), form.getIsAll());
        return ok(devices);
    }

    /**
     * 个人设备模糊查询列表
     * @param form
     * @return
     */
    @ApiOperation(value="个人设备模糊查询列表")
    @ResponseBody
    @GetMapping(value = "/private/search")
    public ResultVO myEnableSearch(@Valid DeviceSearchForm form) throws ObjectNotExistsException, AuthorizedException {
        Set<String> deviceIds = deviceOnlineMgr.allOnlineDevices();
        User user = userService.findByUsername(getCurrentUsername());
        List<PrivateDeviceDTO> privateDeviceDTOList = deviceService.searchPrivateList(deviceIds, user.getId(), form.getBrand(), form.getOsVersion(), form.getResolution(), form.getIsAll());
        // 枚举转换器，可根据需要添加
        ConvertUtils.register(new Converter() {
            @Override
            public Object convert(Class type, Object o) {
                return DB.PhoneOS.valueOf((Integer) o);
            }
        }, DB.PhoneOS.class);
        List<PrivateDeviceVO> result = buildVOs(privateDeviceDTOList, PrivateDeviceVO.class);
        return ok(result);
    }

    @ApiOperation(value="在线Android设备的分类，各个维度", notes = "")
    @ResponseBody
    @GetMapping(value = "/category/android")
    public ResultVO category() throws ObjectNotExistsException {
        Set<String> deviceIds = deviceOnlineMgr.allOnlineDevices();
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
    public ResultVO checkUsable(@RequestBody DeviceBatchCheckForm form) throws ObjectNotExistsException {
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

}
