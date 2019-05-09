package com.testwa.distest.client2.web;

import com.testwa.distest.client2.support.android.AndroidDeviceStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * Created by wen on 16/8/27.
 */
@Slf4j
@Order(value=1)
@Component
public class EnvCheck implements CommandLineRunner {

    @Override
    public void run(String... strings) throws Exception {
        AndroidDeviceStore.getInstance();
    }
}
