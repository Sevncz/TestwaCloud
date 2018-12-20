package com.testwa.distest.server.service;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.DeviceShareScope;
import com.testwa.distest.server.service.device.service.DeviceShareScopeService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class DeviceShareScopeServiceTest {

    @Autowired
    private DeviceShareScopeService deviceShareScopeService;

    @Test
    public void testInsert(){
        DeviceShareScope ds = new DeviceShareScope();
        ds.setCreateBy(17L);
        ds.setCreateTime(new Date());
        ds.setDeviceId("4205dccb");
        ds.setShareScope(DB.DeviceShareScopeEnum.Private);

        deviceShareScopeService.insert(ds);
    }

    @Test
    public void testUpdatePublic(){
        String deviceId = "4205dccb";
        Long createBy = 17L;
        deviceShareScopeService.updateScope(deviceId, createBy, DB.DeviceShareScopeEnum.Protected);
    }



}
