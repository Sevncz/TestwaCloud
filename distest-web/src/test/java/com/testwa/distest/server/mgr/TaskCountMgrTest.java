package com.testwa.distest.server.mgr;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.server.service.cache.mgr.TaskCountMgr;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-dev.properties")
public class TaskCountMgrTest {

    @Autowired
    private TaskCountMgr taskCountMgr;


    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testIncSubTaskCount(){

        Long taskCode = 0L;
        Integer count = taskCountMgr.getSubTaskCount(taskCode);
        taskCountMgr.incrSubTaskCount(taskCode);
        Integer count1 = taskCountMgr.getSubTaskCount(taskCode);
        Assert.assertEquals(String.valueOf(count + 1), String.valueOf(count1));
        taskCountMgr.decrSubTaskCount(taskCode);
        Integer count2 = taskCountMgr.getSubTaskCount(taskCode);
        Assert.assertEquals(String.valueOf(count), String.valueOf(count2));
    }

}
