package com.example.demo;

import com.example.demo.server.WebSocketClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication implements ApplicationRunner {

    @Autowired
    private WebSocketClientFactory webSocketClientFactory;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        // 项目启动的时候打开websocket连接
        webSocketClientFactory.retryOutCallWebSocketClient();
    }
}
