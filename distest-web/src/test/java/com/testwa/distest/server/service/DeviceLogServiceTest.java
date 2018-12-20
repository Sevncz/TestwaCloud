package com.testwa.distest.server.service;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.DeviceLog;
import com.testwa.distest.server.service.device.service.DeviceLogService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author wen
 * @create 2018-12-19 17:29
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class DeviceLogServiceTest {

    @Autowired
    private DeviceLogService deviceLogService;

    @Test
    @Transactional
    @Rollback
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testInsert() {
        DeviceLog deviceLog = new DeviceLog();
        deviceLog.setDeviceId("oppo");
        deviceLog.setProjectId(15L);
        deviceLog.setRunning(true);
        deviceLog.setUserCode("u_xxxx");
        deviceLog.setLogType(DB.DeviceLogType.DEBUG);
        deviceLog.setStartTime(System.currentTimeMillis());
        deviceLog.setEnabled(true);
        Long deviceLogId = deviceLogService.insert(deviceLog);

        DeviceLog deviceLog1 = deviceLogService.get(deviceLogId);
        Assert.assertNotNull(deviceLog1);
    }


    @Test
    @Transactional
    @Rollback
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testUpdate() {
        DeviceLog deviceLog = new DeviceLog();
        deviceLog.setDeviceId("oppo");
        deviceLog.setProjectId(15L);
        deviceLog.setRunning(true);
        deviceLog.setUserCode("u_xxxx");
        deviceLog.setLogType(DB.DeviceLogType.DEBUG);
        deviceLog.setStartTime(System.currentTimeMillis());
        deviceLog.setEnabled(true);
        Long deviceLogId = deviceLogService.insert(deviceLog);

        DeviceLog deviceLog1 = deviceLogService.get(deviceLogId);
        long t = System.currentTimeMillis();
        deviceLog1.setEndTime(t);
        deviceLogService.update(deviceLog1);

        DeviceLog deviceLog2 = deviceLogService.get(deviceLogId);

        Assert.assertEquals(deviceLog2.getEndTime(), t);
    }

}
