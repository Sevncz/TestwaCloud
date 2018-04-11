package com.testwa.distest.server.service;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.service.task.service.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class TaskServiceTest {

    @Autowired
    private TaskService taskService;


    @Test
    public void testFindOne(){
        Task task = taskService.findOne(30l);
        System.out.println(task.toString());
    }
}
