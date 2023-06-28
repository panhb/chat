package com.demo.chat.websocket;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.demo.chat.common.Constant;
import com.demo.chat.model.po.UserPO;
import com.demo.chat.model.vo.MessageVO;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

/**
 * @author hongbo.pan
 * @date 2023/6/28
 */
@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static Map<String, WebSocketSession> webSocketMap = Maps.newConcurrentMap();

    private static final String PING = "PING";

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        String message = textMessage.getPayload();
        log.info("message={}", message);
        if (StrUtil.isNotBlank(message)) {
            if (PING.equals(message)) {
                UserPO user = (UserPO) session.getAttributes().get(Constant.USER_PO);
                sendMessage(user.getUsername(), "PONG");
                log.info("心跳");
            } else {
                MessageVO vo = JSON.parseObject(message, MessageVO.class);
                sendMessage(vo.getToUser(), vo.getMessage());
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 连接建立后的处理
        UserPO user = (UserPO) session.getAttributes().get(Constant.USER_PO);
        if (ObjectUtil.isNotNull(user)) {
            webSocketMap.put(user.getUsername(), session);
            log.info("开始监听:" + user.getUsername() + ",当前人数为:" + webSocketMap.keySet().size());
        } else {
            log.error("afterConnectionEstablished 未登录");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 连接关闭后的处理
        log.info("afterConnectionClosed CloseStatus={}", status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        // 连接错误后的处理
        log.error("handleTransportError", exception);
    }

    /**
     * sendMessage
     * @param sid
     * @param message
     */
    public static void sendMessage(String sid, String message) {
        if (webSocketMap.containsKey(sid)) {
            log.info("推送消息到窗口={}，推送内容={}", sid,  message);
            WebSocketSession webSocketSession = webSocketMap.get(sid);
            if (ObjectUtil.isNotNull(webSocketSession)) {
                try {
                    webSocketSession.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    log.error("websocket推送错误，sid={}", sid, e);
                }
            }
        }
    }
}
