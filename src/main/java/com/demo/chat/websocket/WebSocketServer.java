package com.prolog.rdc.tms.service.message.impl;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
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
@ServerEndpoint("/messageJob/websocket/{channel}/{userId}")
public class WebSocketServer {

    private static Map<String, WebSocketServer> webSocketMap = Maps.newConcurrentMap();

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
    public void onOpen(Session session, @PathParam("channel") String channel, @PathParam("userId") String userId) {
        this.session = session;
        this.sid = channel + "-" + userId;
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
     * @param userId
     */
    public static void sendMessage(String message, String userId) {
        String pcSid = "pc-" + userId;
        String wechatSid = "wechat-" + userId;
        sendMessageToClient(message, pcSid);
        sendMessageToClient(message, wechatSid);
    }

    /**
     * sendMessageToClient
     * @param message
     * @param sid
     */
    private static void sendMessageToClient(String message, String sid) {
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
