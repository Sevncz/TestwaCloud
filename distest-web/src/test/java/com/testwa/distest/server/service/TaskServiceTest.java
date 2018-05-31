package com.testwa.distest.server.service;

import com.alibaba.fastjson.JSON;
import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.entity.TaskDevice;
import com.testwa.distest.server.service.task.service.TaskDeviceService;
import com.testwa.distest.server.service.task.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-dev.properties")
public class TaskServiceTest {

    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskDeviceService taskDeviceService;


    @Test
    public void testFindOne(){
        Task task = taskService.findOne(30l);
        System.out.println(task.toString());
    }
    @Test
    public void testFindByCode(){
        Task task = taskService.findByCode(10466432571670528L);
        System.out.println(task.toString());
    }
    @Test
    public void testDeleteTaskDeviceByTaskId(){
        taskDeviceService.deleteTaskDevice(10466432571670528L);
    }
    @Test
    public void testFindByTaskCode(){
        List<TaskDevice> tds = taskDeviceService.findByTaskCode(10466432571670528L);
        log.info(JSON.toJSONString(tds));
    }
}
