package com.testwa.distest.server.web;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.server.mongo.event.TaskOverEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class EventListenerTest {

    @Autowired
    private WebApplicationContext context;

    @Test
    @WithAnonymousUser
    public void testSendGameOverEvent(){

        context.publishEvent(new TaskOverEvent(this, 31l));
    }

}
