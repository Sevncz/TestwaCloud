package com.testwa.distest.client.web.startup;

import com.testwa.distest.client.android.ADBTools;
import com.testwa.distest.client.android.JadbTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * @author wen
 * @create 2019-05-15 18:58
 */
@Slf4j
@Order(value=2)
@Component
public class JadbConnectCommandLine implements CommandLineRunner {
    @Override
    public void run(String... strings) throws Exception {
        ADBTools.restartAdb();
        JadbTools.init();
    }
}
