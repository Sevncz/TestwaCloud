package com.testwa.distest.client.web.startup;

import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.engineio.client.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by wen on 16/8/14.
 */
@Component
public class TestwaSocketConnect implements CommandLineRunner {
    private static Logger LOG = LoggerFactory.getLogger(TestwaSocketConnect.class);
    private final Socket socket;

    @Autowired
    public TestwaSocketConnect(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run(String... strings) throws Exception {
        LOG.info("Socket connecting");

//        socket.on(Manager.EVENT_TRANSPORT, args -> {
//            Transport transport = (Transport)args[0];
//            transport.on(Transport.EVENT_REQUEST_HEADERS, args1 -> {
//                @SuppressWarnings("unchecked")
//                Map<String, String> headers = (Map<String, String>) args1[0];
//                // set header
//                headers.put("X-SocketIO", "hi");
//                LOG.info(" add header !!!!");
//            }).on(Transport.EVENT_RESPONSE_HEADERS, args1 -> {
//                @SuppressWarnings("unchecked")
//                Map<String, String> headers = (Map<String, String>) args1[0];
//                // get header
//                String value = headers.get("X-SocketIO");
//            });
//        });
        socket.connect();
    }
}
