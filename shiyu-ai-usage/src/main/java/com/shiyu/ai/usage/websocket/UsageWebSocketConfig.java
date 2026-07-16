package com.shiyu.ai.usage.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置
 * <p>
 * 注册用量实时推送端点 {@code /ws/usage}。
 * 前端连接：new WebSocket('ws://host:9000/ws/usage')
 */
@Configuration
@EnableWebSocket
public class UsageWebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(usageWebSocketHandler(), "/ws/usage")
                .setAllowedOriginPatterns("*");
    }

    @Bean
    public UsageWebSocketHandler usageWebSocketHandler() {
        return new UsageWebSocketHandler();
    }
}
