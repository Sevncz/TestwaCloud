package com.testwa.distest.client.web.startup;

import com.testwa.distest.client.component.debug.IOSDebugServer;
import com.testwa.distest.client.model.AgentInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * @author wen
 * @create 2019-06-13 18:58
 */
@Slf4j
@Order(value= Ordered.HIGHEST_PRECEDENCE)
@Component
public class IOSRemoteConnectCommandLine implements CommandLineRunner {

    @Override
    public void run(String... strings) throws Exception {
        AgentInfo agentInfo = AgentInfo.getAgentInfo();
        if("Mac OS X".equals(agentInfo.getOsName())){
            IOSDebugServer debugServer = new IOSDebugServer(8555);
            debugServer.start();
        }
    }
}
