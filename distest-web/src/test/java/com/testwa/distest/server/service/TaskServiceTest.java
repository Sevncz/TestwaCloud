package com.testwa.distest.server.service;

import com.alibaba.fastjson.JSON;
import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.entity.SubTask;
import com.testwa.distest.server.service.task.service.SubTaskService;
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
@TestPropertySource(locations="classpath:application-test.properties")
public class TaskServiceTest {

    @Autowired
    private TaskService taskService;
    @Autowired
    private SubTaskService subTaskService;


    @Test
    public void testFindOne(){
        Task task = taskService.get(30l);
        System.out.println(task.toString());
    }
    @Test
    public void testFindByCode(){
        Task task = taskService.findByCode(10466432571670528L);
        System.out.println(task.toString());
    }
    @Test
    public void testDeleteTaskDeviceByTaskId(){
        subTaskService.deleteTaskDevice(10466432571670528L);
    }
    @Test
    public void testFindByTaskCode(){
        List<SubTask> tds = subTaskService.findByTaskCode(10466432571670528L);
        log.info(JSON.toJSONString(tds));
    }
}
