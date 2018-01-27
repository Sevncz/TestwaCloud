package com.testwa.distest.server.startup;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by wen on 16/8/30.
 */
@Slf4j
@Component
public class SocketDestoryRunner implements DisposableBean {

    private final SocketIOServer server;

    @Autowired
    public SocketDestoryRunner(SocketIOServer server) {
        this.server = server;
    }

    @Override
    public void destroy() throws Exception {
        this.server.stop();
        log.info("websocket server was stop");
    }
}
