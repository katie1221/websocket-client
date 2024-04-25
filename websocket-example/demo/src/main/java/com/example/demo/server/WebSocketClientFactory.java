package com.example.demo.server;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * WebSocketClient
 * @author qzz
 * @date 2024/4/25
 */
@Component
@Slf4j
@Data
public class WebSocketClientFactory {

    public static final String outCallWebSockertUrl = "wss://elinktech.link:10007";

    private WebSocketClient outCallWebSocketClientHolder;

    /**
     * 创建websocket对象
     *
     * @return WebSocketClient
     * @throws URISyntaxException
     */
    private WebSocketClient createNewWebSocketClient() throws URISyntaxException {
        WebSocketClient webSocketClient = new WebSocketClient(new URI(outCallWebSockertUrl)) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                //发送token 认证
                Map<String,String> data =new HashMap<>();
                data.put("token", "填写token");
                send(JSONObject.toJSONString(data));
            }

            @Override
            public void onMessage(String response) {
                log.info("-------- 接收到服务端数据： " + response + "--------");
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                log.info("关闭连接");
                retryOutCallWebSocketClient();
            }

            @Override
            public void onError(Exception e) {
                log.info("连接异常");
                retryOutCallWebSocketClient();
            }
        };
        webSocketClient.connect();
        return webSocketClient;
    }

    /**
     * 项目启动或连接失败的时候打开新链接,进行连接认证
     * 需要加同步，不然会创建多个连接
     */
    public synchronized WebSocketClient retryOutCallWebSocketClient() {
        try {
            // 关闭旧的websocket连接, 避免占用资源
            WebSocketClient oldOutCallWebSocketClientHolder = this.getOutCallWebSocketClientHolder();
            if (null != oldOutCallWebSocketClientHolder) {
                log.info("关闭旧的websocket连接");
                oldOutCallWebSocketClientHolder.close();
            }

            log.info("打开新的websocket连接，并进行认证");
            WebSocketClient webSocketClient = this.createNewWebSocketClient();
            //发送token 认证
            Map<String,String> data =new HashMap<>();
            data.put("token", "填写token");
            this.sendMsg(webSocketClient, JSONObject.toJSONString(data));
            // 每次创建新的就放进去
            this.setOutCallWebSocketClientHolder(webSocketClient);
            return webSocketClient;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 发送消息
     * 注意： 要加超时设置，避免很多个都在同时超时占用资源
     *
     * @param webSocketClient 指定的webSocketClient
     * @param message         消息
     */
    public void sendMsg(WebSocketClient webSocketClient, String message) {
        log.info("websocket向服务端发送消息，消息为：{}", message);
        long startOpenTimeMillis = System.currentTimeMillis();
        while (!webSocketClient.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {
            log.debug("正在建立通道，请稍等");
            long currentTimeMillis = System.currentTimeMillis();
            if(currentTimeMillis - startOpenTimeMillis >= 5000) {
                log.error("超过5秒钟还未打开连接，超时，不再等待");
                return;
            }
        }
        webSocketClient.send(message);
    }
}
