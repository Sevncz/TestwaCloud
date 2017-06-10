package com.testwa.distest.server.run;

import com.testwa.core.WebsocketEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 启动的时候对redis的部分key进行初始化动作
 * Created by wen on 16/9/7.
 */
@Component
public class RedisInitRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(TestwaScheduledRunner.class);

    @Autowired
    private StringRedisTemplate template;


    @Override
    public void run(String... strings) throws Exception {
        template.delete(WebsocketEvent.CONNECT_SESSION);
        template.delete(WebsocketEvent.DEVICE);

        Set<String> keys = template.keys("client.session.devices.*");
        for(String key : keys){
            template.delete(key);
        }
        log.info("clean redis key");
    }
}
