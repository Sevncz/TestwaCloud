package com.testwa.distest.server.mvc.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


/**
 * Created by wen on 19/08/2017.
 */
@Component
public class GameOverListener implements ApplicationListener<GameOverEvent> {
    private static Logger log = LoggerFactory.getLogger(GameOverListener.class);

    @Async
    @Override
    public void onApplicationEvent(GameOverEvent e) {
        log.info("start...");
        // 根据前端需求开始统计报告
    }

}
