package com.testwa.distest.server.service;

import com.testwa.distest.WebServerApplication;
import com.testwa.distest.server.service.device.form.DeviceAuthListForm;
import com.testwa.distest.server.service.device.form.DeviceAuthNewForm;
import com.testwa.distest.server.service.device.form.DeviceAuthRemoveForm;
import com.testwa.distest.server.service.device.service.DeviceAuthService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = WebServerApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class DeviceAuthServiceTest {

    @Autowired
    private DeviceAuthService deviceAuthService;


    @Test
    public void testInsert(){
        DeviceAuthNewForm form = new DeviceAuthNewForm();
        form.setDeviceId("123456");
        form.setUserIds(Arrays.asList(5l));
        deviceAuthService.insert(form, 3l);
    }
    @Test
    public void testRemoveSomeFromDevice(){
        DeviceAuthRemoveForm form = new DeviceAuthRemoveForm();
        form.setDeviceId("123456");
        form.setUserIds(Arrays.asList(5l));
        deviceAuthService.removeSomeFromDevice(form, 3l);
    }



}
