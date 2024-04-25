package com.example.demo.controller;

import com.example.demo.server.WebSocketClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试类
 * @author qzz
 * @date 2024/4/25
 */
@RestController
public class TestController {

    @Autowired
    private WebSocketClientFactory webSocketClientFactory;

    @PostMapping("/send")
    public void send(){
        String msg = "{\"node_num\": \"HCBB24400011\",\"control_data\": { \"control_cmd\": \"FG_control\",\"talk_channel\": \"1234\" }, \"id\": \"17138435580aad\" }";
        webSocketClientFactory.sendMsg(webSocketClientFactory.getOutCallWebSocketClientHolder(), msg);
    }
}
