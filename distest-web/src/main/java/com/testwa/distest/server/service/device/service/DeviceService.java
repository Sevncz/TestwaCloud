package com.testwa.distest.server.service.device.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.vo.PageResultVO;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.IOSDeviceDict;
import com.testwa.distest.server.service.device.dao.IDeviceDAO;
import com.testwa.distest.server.service.device.dao.IIOSDeviceDictDAO;
import com.testwa.distest.server.service.device.dto.DeviceOneCategoryResultDTO;
import com.testwa.distest.server.service.device.form.DeviceListForm;
import com.testwa.distest.server.web.device.vo.DeviceCategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * Created by wen on 20/10/2017.
 */
@Slf4j
@Service
public class DeviceService {

    @Autowired
    private IDeviceDAO deviceDAO;
    @Autowired
    private IIOSDeviceDictDAO iosDeviceDictDAO;

    public Device findByDeviceId(String deviceId) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("deviceId", deviceId);
        List<Device> list = deviceDAO.findOnlineList(queryMap);
        if (list.size() > 0){
            return list.get(0);
        }
        return null;
    }

    public void insertAndroid(Device entity) {
        entity.setCreateTime(new Date());
        deviceDAO.insertAndroid(entity);
    }

    public void updateStatus(String deviceId, DB.PhoneOnlineStatus status) {
        deviceDAO.updateOnlineStatus(deviceId, status);
    }

    public void updateWorkStatus(String deviceId, DB.DeviceWorkStatus status) {
        deviceDAO.updateWorkStatus(deviceId, status);
    }

    public void updateAndroid(Device entity) {
        entity.setUpdateTime(new Date());
        deviceDAO.updateAndroid(entity);
    }

    public List<Device> findAll(List<String> deviceIds) {
        return deviceDAO.findAll(deviceIds);
    }
    public Device findOne(String deviceId) {
        return deviceDAO.findOne(deviceId);
    }

    /**
     * 返回所有设备列表，并通过 onlineDeviceList 标记设备的准确状态
     * @param onlineDeviceList  在线设备列表
     * @param pageForm
     * @return
     */
    public PageResultVO<Device> findByPage(Set<String> onlineDeviceList, DeviceListForm pageForm) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("brand", pageForm.getBrand());
        queryMap.put("model", pageForm.getModel());
        queryMap.put("deviceId", pageForm.getDeviceId());
        queryMap.put("onlineStatus", pageForm.getOnlineStatus());
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        if(StringUtils.isBlank(pageForm.getOrderBy()) ){
            pageForm.getPage().setOrderBy("id");
        }
        if(StringUtils.isBlank(pageForm.getOrder()) ){
            pageForm.getPage().setOrder("desc");
        }
        PageHelper.orderBy(pageForm.getOrderBy() + " " + pageForm.getOrder());
        List<Device> deviceList = deviceDAO.findListByOnlineDevice(queryMap, onlineDeviceList);
        PageInfo<Device> info = new PageInfo(deviceList);
        PageResultVO<Device> pr = new PageResultVO<>(info.getList(), info.getTotal());
        return pr;
    }

    /**
     * 返回所有设备列表，并通过 onlineDeviceList 标记设备的准确状态
     * @param onlineDeviceList  在线设备列表
     * @param form
     * @return
     */
    public List<Device> findList(Set<String> onlineDeviceList, DeviceListForm form) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("brand", form.getBrand());
        queryMap.put("model", form.getModel());
        queryMap.put("deviceId", form.getDeviceId());
        queryMap.put("onlineStatus", form.getOnlineStatus());
        return deviceDAO.findListByOnlineDevice(queryMap, onlineDeviceList);
    }

    public List<Device> findOnlineList(Set<String> deviceIds) {
        return findOnlineList(deviceIds, null);
    }

    public List<Device> findOnlineList(Set<String> onlineDevIds, DeviceListForm form) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("deviceIdList", onlineDevIds);
        if(form != null){
            queryMap.put("model", form.getModel());
            queryMap.put("brand", form.getBrand());
            if(StringUtils.isNotBlank(form.getDeviceId())){
                queryMap.remove("deviceIdList");
            }
            queryMap.put("deviceId", form.getDeviceId());
        }
        return deviceDAO.findOnlineList(queryMap);
    }

    public PageResultVO<Device> findOnlinePage(Set<String> onlineDevIds, DeviceListForm pageForm) {
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        if(StringUtils.isBlank(pageForm.getOrderBy()) ){
            pageForm.getPage().setOrderBy("id");
        }
        if(StringUtils.isBlank(pageForm.getOrder()) ){
            pageForm.getPage().setOrder("desc");
        }
        PageHelper.orderBy(pageForm.getOrderBy() + " " + pageForm.getOrder());
        List<Device> deviceList = findOnlineList(onlineDevIds, pageForm);
        PageInfo<Device> info = new PageInfo(deviceList);
        PageResultVO<Device> pr = new PageResultVO<>(info.getList(), info.getTotal());
        return pr;
    }

    public List<Device> fetchList(Set<String> onlineDeviceList, Long createBy) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("createBy", createBy);
        queryMap.put("onlineDeviceList", onlineDeviceList);
        return deviceDAO.fetchList(queryMap);
    }

    public void release(String deviceId) {
        deviceDAO.updateWorkStatus(deviceId, DB.DeviceWorkStatus.FREE);
    }

    public void work(String deviceId) {
        deviceDAO.updateWorkStatus(deviceId, DB.DeviceWorkStatus.BUSY);
    }

    public DeviceCategoryVO getCategory(Set<String> deviceIds) {

        DeviceCategoryVO vo = new DeviceCategoryVO();
        List<DeviceOneCategoryResultDTO> dto = deviceDAO.getResolutionCategory(deviceIds);
        dto.forEach( d -> {
            vo.getResolution().add(d.getName());
        });

        dto = deviceDAO.getOSVersionCategory(deviceIds);
        dto.forEach( d -> {
            vo.getOsVersion().add(d.getName());
        });

        dto = deviceDAO.getBrandCategory(deviceIds);
        dto.forEach( d -> {
            vo.getBrand().add(d.getName());
        });

        return vo;
    }

    public List<Device> findCloudList(Set<String> deviceIds, String brand, String osVersion, String resolution, Boolean isAll) {
        if(deviceIds == null || deviceIds.size() == 0) {
            return new ArrayList<>();
        }
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("deviceIdList", deviceIds);
        if(StringUtils.isNotBlank(brand)){
            queryMap.put("brand", brand);
        }
        if(StringUtils.isNotBlank(osVersion)){
            queryMap.put("osVersion", osVersion);
        }
        if(StringUtils.isNotBlank(resolution)){
            String[] wh = resolution.split("x");
            if(wh.length == 2) {
                queryMap.put("width", wh[0].trim());
                queryMap.put("height", wh[1].trim());
            }
        }
        if(isAll != null && !isAll) {
            // 只看空闲设备
            queryMap.put("workStatus", DB.DeviceWorkStatus.FREE);
            queryMap.put("debugStatus", DB.DeviceDebugStatus.FREE);
        }
        return deviceDAO.findOnlineList(queryMap);
    }

    public List<Device> searchCloudList(Set<String> deviceIds, String brand, String osVersion, String resolution, Boolean isAll) {
        if(deviceIds == null || deviceIds.size() == 0) {
            return new ArrayList<>();
        }
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("deviceIdList", deviceIds);
        if(StringUtils.isNotBlank(brand)){
            queryMap.put("brand", brand);
        }
        if(StringUtils.isNotBlank(osVersion)){
            queryMap.put("osVersion", osVersion);
        }
        if(StringUtils.isNotBlank(resolution)){
            String[] wh = resolution.split("x");
            if(wh.length == 2) {
                queryMap.put("width", wh[0].trim());
                queryMap.put("height", wh[1].trim());
            }
        }
        if(isAll != null && !isAll) {
            // 只看空闲设备
            queryMap.put("workStatus", DB.DeviceWorkStatus.FREE);
            queryMap.put("debugStatus", DB.DeviceDebugStatus.FREE);
        }
        return deviceDAO.searchCloudList(queryMap);
    }

    public PageResultVO<Device> findCloudPage(Set<String> deviceIds, String brand, String osVersion, String resolution, Boolean isAll) {

        return null;
    }

    public void debugging(String deviceId) {
        deviceDAO.updateDebugStatus(deviceId, DB.DeviceDebugStatus.DEBUGGING);
    }

    public void debugFree(String deviceId) {
        deviceDAO.updateDebugStatus(deviceId, DB.DeviceDebugStatus.FREE);
    }

    @Cacheable("ios_dict")
    public IOSDeviceDict getIOSDict(String productType) {
        return iosDeviceDictDAO.findByProductType(productType);
    }

    /**
     *@Description: 获得所有在工作中的设备
     *@Param: []
     *@Return: java.util.List<com.testwa.distest.server.entity.Device>
     *@Author: wen
     *@Date: 2018/8/10
     */
    public List<Device> findAllInWrok() {
        return deviceDAO.findAllInWrok();
    }
}
