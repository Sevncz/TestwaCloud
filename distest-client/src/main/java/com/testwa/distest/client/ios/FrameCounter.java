package com.testwa.distest.client.ios;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 帧率计算器
 * @author wen
 * @create 2019-04-29 19:35
 */
@Slf4j
@Data
public class FrameCounter {
    private static final Long SECOND_1 = 1000L;
    private static final int MAX_COUNT = 5;
    private Long startTime;
    private AtomicReference<Integer> count ;

    public FrameCounter(){
        this.count = new AtomicReference<>(0);
        this.startTime = System.currentTimeMillis();
    }

    public boolean filter(){
        Long currentTime = System.currentTimeMillis();
        if((currentTime - startTime) > SECOND_1/MAX_COUNT){
            startTime = currentTime;
            return true;
        }
        return false;
    }

}
