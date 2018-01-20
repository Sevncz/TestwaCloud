package com.testwa.distest.server.startup;

import com.corundumstudio.socketio.SocketIOServer;
import io.netty.util.concurrent.Future;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class SrvRunner implements CommandLineRunner {

    private final SocketIOServer server;

    @Autowired
    public SrvRunner(SocketIOServer server) {
        this.server = server;
    }

    @Override
    public void run(String... args) throws Exception {
        Future future = server.startAsync();

        new Thread(() ->{
            Future f = future;
            while(true){
                try {
                    Thread.sleep(60*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(!f.isSuccess()){
                    f = server.startAsync();
                }
            }
        }).start();
    }

}