package com.testwa.distest.server.service;

import com.testwa.core.base.form.RequestListBase;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.WebServerApplication;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.DeviceAndroid;
import com.testwa.distest.server.service.device.form.DeviceListForm;
import com.testwa.distest.server.service.device.service.DeviceService;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

@Log4j2
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = WebServerApplication.class)
public class DeviceServiceTest {

    @Autowired
    private DeviceService deviceService;


    @Test
    public void testInsertAndroid(){
        DeviceAndroid d = new DeviceAndroid();
        d.setCpuabi("cpu-v7");
        d.setModel("Google");
        d.setBrand("Google");
        d.setDeviceId("123456");
        d.setHeight("1080");
        d.setWidth("720");
        deviceService.insertAndroid(d);
        log.info(d);
    }

    @Test
    public void testUpdate(){
        DeviceAndroid d = new DeviceAndroid();
        d.setCpuabi("cpu-v9");
        d.setModel("Google 7");
        d.setBrand("Google");
        d.setDeviceId("123456");
        d.setHeight("1080p");
        d.setWidth("720p");
        deviceService.updateAndroid(d);
        log.info(d);
    }

    @Test
    public void testFindByDeviceId(){
        Device d = deviceService.findByDeviceId("123456");
        log.info(d);
    }

    @Test
    public void testFindByPage(){
        DeviceListForm form = new DeviceListForm();
        RequestListBase.Page page = form.getPage();
        page.setPageNo(1);
        page.setPageSize(10);
        form.setPage(page);
        PageResult<Device> devices = deviceService.findByPage(form);
        log.info(devices.getPages());
    }

    @Test
    public void testUpdateStatus(){
        String deviceId = "123456";
        deviceService.updateStatus(deviceId, DB.PhoneOnlineStatus.OFFLINE);
    }

    @Test
    public void testFetchList(){
        List<Device> deviceList = deviceService.fetchList(3l, Arrays.asList("123456", "223456", "323456"));
        for(Device d : deviceList){
            log.info(d.getDeviceAuths());
            log.info(d.toString());
        }
    }

}
