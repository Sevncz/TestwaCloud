package com.testwa.distest.server.service.device.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.service.device.dao.IDeviceDAO;
import com.testwa.distest.server.service.device.form.DeviceListForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    public void updateWorkStatus(String deviceId, DB.PhoneWorkStatus status) {
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
    public PageResult<Device> findByPage(Set<String> onlineDeviceList, DeviceListForm pageForm) {
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
        PageResult<Device> pr = new PageResult<>(info.getList(), info.getTotal());
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

    public PageResult<Device> findOnlinePage(Set<String> onlineDevIds, DeviceListForm pageForm) {
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
        PageResult<Device> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    public List<Device> fetchList(Set<String> onlineDeviceList, Long createBy) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("createBy", createBy);
        queryMap.put("onlineDeviceList", onlineDeviceList);
        return deviceDAO.fetchList(queryMap);
    }

    public List<Device> findAllDeviceAndroid(List<String> deviceIds) {
        return deviceDAO.findAllDeviceAndroid(deviceIds);
    }

    public void release(String deviceId) {
        deviceDAO.updateWorkStatus(deviceId, DB.PhoneWorkStatus.FREE);
    }

    public void work(String deviceId) {
        deviceDAO.updateWorkStatus(deviceId, DB.PhoneWorkStatus.BUSY);
    }
}
