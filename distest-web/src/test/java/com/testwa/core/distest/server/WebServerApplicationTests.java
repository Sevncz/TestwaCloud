package com.testwa.distest.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WebServerApplication.class)
public class WebServerApplicationTests {


    @Test
    public void testInit(){
        System.out.println("2121");
    }

}
