package com.testwa.distest.server.service;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.server.service.device.service.DeviceSharerService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class DeviceSharerServiceTest {

    @Autowired
    private DeviceSharerService deviceSharerService;

    @Test
    public void testInsertList(){
        List<Long> toUserIds = new ArrayList<>();
        toUserIds.add(7L);
        toUserIds.add(16L);

        String deviceId = "4205dccb";
        Long ownerId = 17L;

        deviceSharerService.insertList(deviceId, ownerId, new HashSet<>(toUserIds));
    }

    @Test
    public void testRemoveList(){
        List<Long> entityIds = new ArrayList<>();
        entityIds.add(3L);

        deviceSharerService.removeList(entityIds);
    }



}
