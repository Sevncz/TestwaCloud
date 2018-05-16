package com.testwa.distest.server.startup;

import com.corundumstudio.socketio.SocketIOServer;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SocketIOServerRunner implements CommandLineRunner, DisposableBean {

    private final SocketIOServer server;

    @Autowired
    public SocketIOServerRunner(SocketIOServer server) {
        this.server = server;
    }

    @Override
    public void run(String... args) throws Exception {
        startDaemonAwaitThread();
    }

    private void startDaemonAwaitThread() {
        Thread awaitThread = new Thread(()->{
            Future f = this.server.startAsync();
            while(true){
                try {
                    TimeUnit.MILLISECONDS.sleep(10*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(!f.isSuccess()){
                    f = server.startAsync();
                }
            }
        });
        awaitThread.setDaemon(false);
        awaitThread.start();
    }

    @Override
    public void destroy() throws Exception {
        this.server.stop();
    }
}