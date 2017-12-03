package com.testwa.distest.server.service.device.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.DeviceAndroid;
import com.testwa.distest.server.service.device.dao.IDeviceDAO;
import com.testwa.distest.server.service.device.form.DeviceListForm;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * Created by wen on 20/10/2017.
 */
@Log4j2
@Service
public class DeviceService {

    @Autowired
    private IDeviceDAO deviceDAO;


    public Device findByDeviceId(String deviceId) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("deviceId", deviceId);
        List<Device> list = deviceDAO.findBy(queryMap);
        if (list.size() > 0){
            return list.get(0);
        }
        return null;
    }

    public void insertAndroid(DeviceAndroid entity) {
        entity.setCreateTime(new Date());
        deviceDAO.insertAndroid(entity);
    }

    public void updateStatus(String deviceId, DB.PhoneOnlineStatus status) {
        deviceDAO.updateStatus(deviceId, status);
    }

    public void updateAndroid(DeviceAndroid entity) {
        entity.setUpdateTime(new Date());
        deviceDAO.updateAndroid(entity);
    }

    public List<Device> findAll(List<String> deviceIds) {
        return deviceDAO.findAll(deviceIds);
    }
    public Device findOne(String deviceId) {
        return deviceDAO.findOne(deviceId);
    }

    public PageResult<Device> findByPage(DeviceListForm pageForm) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("brand", pageForm.getBrand());
        queryMap.put("model", pageForm.getModel());
        queryMap.put("deviceId", pageForm.getDeviceId());
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        if(StringUtils.isBlank(pageForm.getOrderBy()) ){
            pageForm.getPage().setOrderBy("id");
        }
        if(StringUtils.isBlank(pageForm.getOrder()) ){
            pageForm.getPage().setOrder("desc");
        }
        PageHelper.orderBy(pageForm.getOrderBy() + " " + pageForm.getOrder());
        List<Device> deviceList = deviceDAO.findBy(queryMap);
        PageInfo<Device> info = new PageInfo(deviceList);
        PageResult<Device> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    public List<Device> findByDeviceIds(Set<String> deviceIds, DeviceListForm form) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("deviceIdList", deviceIds);
        if(form != null){
            queryMap.put("model", form.getModel());
            queryMap.put("brand", form.getBrand());
            if(StringUtils.isNotBlank(form.getDeviceId())){
                queryMap.remove("deviceIdList");
            }
            queryMap.put("deviceId", form.getDeviceId());
        }
        return deviceDAO.findBy(queryMap);
    }

    public PageResult<Device> findByDeviceIdsPage(Set<String> deviceIds, DeviceListForm pageForm) {
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        if(StringUtils.isBlank(pageForm.getOrderBy()) ){
            pageForm.getPage().setOrderBy("id");
        }
        if(StringUtils.isBlank(pageForm.getOrder()) ){
            pageForm.getPage().setOrder("desc");
        }
        PageHelper.orderBy(pageForm.getOrderBy() + " " + pageForm.getOrder());
        List<Device> deviceList = findByDeviceIds(deviceIds, pageForm);
        PageInfo<Device> info = new PageInfo(deviceList);
        PageResult<Device> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    public List<Device> fetchList(Long createBy, Collection deviceIds) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("createBy", createBy);
        queryMap.put("deviceIdList", deviceIds);
        return deviceDAO.fetchList(queryMap);
    }
}
