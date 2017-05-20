package com.testwa.distest.server.run;

import com.corundumstudio.socketio.SocketIOServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

/**
 * Created by wen on 16/8/30.
 */
@Component
public class SocketDestoryRunner implements DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(SocketDestoryRunner.class);

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
