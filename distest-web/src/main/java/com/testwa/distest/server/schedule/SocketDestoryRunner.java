package com.testwa.distest.server.schedule;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by wen on 16/8/30.
 */
@Log4j2
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
