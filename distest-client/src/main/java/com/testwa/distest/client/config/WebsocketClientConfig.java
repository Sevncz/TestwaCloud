package com.testwa.distest.client.config;

import com.testwa.distest.client.control.client.MainClient;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by wen on 16/9/11.
 */
@Configuration
public class WebsocketClientConfig {

    @Autowired
    private Environment env;

    @Bean
    public Socket socket() throws IOException, URISyntaxException {
        String url = env.getProperty("agent.socket.url");
        String username = env.getProperty("username");
        String password = env.getProperty("password");

        Socket socket = IO.socket(String.format("%s?username=%s&password=%s", url, username, password));
        MainClient.setWs(socket);
        return socket;
    }

    @Bean
    public MainClient app(){
        return new MainClient(env.getProperty("username"), env.getProperty("password"));
    }


}
