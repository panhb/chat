package com.demo.chat.config;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.demo.chat.common.Constant;
import com.demo.chat.utils.SessionUtil;
import com.demo.chat.websocket.ChatWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * @author hongbo.pan
 * @date 2023/6/28
 */
@Slf4j
@EnableWebSocket
@Configuration
public class WebSocketConfigure implements WebSocketConfigurer {

    public static final String WEBSOCKET_TOKEN = "token";

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/chat")
                // 允许跨域
                .setAllowedOrigins("*")
                .addInterceptors(new WebSocketInterceptor());
    }

    private class WebSocketInterceptor implements HandshakeInterceptor {

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Map<String, Object> attributes) {
            // 鉴权操作
            String token = ((ServletServerHttpRequest)request).getServletRequest().getParameter(WEBSOCKET_TOKEN);
            if (StrUtil.isNotBlank(token)) {
                log.info("token={}", token);
                StpUtil.setTokenValue(token);
                if (StpUtil.isLogin()) {
                    attributes.put(Constant.USER_PO, SessionUtil.getUser());
                    return true;
                }
            }
            log.info("token is null");
            return false;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
        }
    }
}

