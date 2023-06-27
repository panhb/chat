package com.demo.chat.websocket;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;

/**
 * @author hongbo.pan
 * @date 2023/4/4
 */
@Slf4j
@Component
@ServerEndpoint("/chat/{userId}")
public class WebSocketServer {

    private static Map<String, WebSocketServer> webSocketMap = Maps.newConcurrentMap();

    private static final String PING = "PING";

    /**
     * 连接会话
     */
    private Session session;

    /**
     * 用户唯一标识
     */
    private String sid;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.session = session;
        this.sid = userId;
        webSocketMap.put(this.sid, this);
        log.info("开始监听:" + this.sid + ",当前人数为:" + webSocketMap.keySet().size());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketMap.remove(this.sid);
        log.info("释放的sid为：" + this.sid + ",当前人数为:" + webSocketMap.keySet().size());
    }

    /**
     * onError
     * @param error
     */
    @OnError
    public void onError(Throwable error) {
        log.error("webSocket发生错误", error);
    }

    /**
     * sendMessage
     * @param message
     */
    @OnMessage
    public void onMessage(String message) {
        log.info("message={}", message);
        if (StrUtil.isNotBlank(message)) {
            if (PING.equals(message)) {
                sendMessage(this.sid, "PONG");
                log.info("心跳");
            } else {
                MessageVO vo = JSON.parseObject(message, MessageVO.class);
                sendMessage(vo.getToUser(), vo.getMessage());
            }
        }
    }

    /**
     * sendMessage
     * @param sid
     * @param message
     */
    public static void sendMessage(String sid, String message) {
        if (webSocketMap.containsKey(sid)) {
            log.info("推送消息到窗口={}，推送内容={}", sid,  message);
            WebSocketServer webSocketServer = webSocketMap.get(sid);
            if (ObjectUtil.isNotNull(webSocketServer)) {
                try {
                    webSocketServer.session.getBasicRemote().sendText(message);
                } catch (Exception e) {
                    log.error("websocket推送错误，sid={}", sid, e);
                }
            }
        }
    }
}
