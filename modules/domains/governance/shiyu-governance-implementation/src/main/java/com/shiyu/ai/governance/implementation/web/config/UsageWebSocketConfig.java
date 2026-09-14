package com.shiyu.ai.governance.implementation.web.config;

import com.shiyu.ai.governance.implementation.web.websocket.UsageWebSocketHandler;
import com.shiyu.ai.governance.implementation.web.websocket.UsageWebSocketService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置
 *
 * <p>注册用量实时推送端点 {@code /ws/usage}。 前端连接：new WebSocket('ws://host:9000/ws/usage')
 */
@Configuration
@EnableWebSocket
public class UsageWebSocketConfig implements WebSocketConfigurer {

    /**
     * {@code registerWebSocketHandlers} 写入或更新当前模块中的业务数据。
     *
     * @param registry 参数值，用于执行当前操作。
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(usageWebSocketHandler(), "/ws/usage").setAllowedOriginPatterns("*");
    }

    /**
     * {@code usageWebSocketHandler} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    public UsageWebSocketHandler usageWebSocketHandler() {
        return new UsageWebSocketHandler();
    }

    /**
     * {@code usageWebSocketService} 执行当前类型定义的业务操作。
     *
     * @param handler 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    public UsageWebSocketService usageWebSocketService(UsageWebSocketHandler handler) {
        return new UsageWebSocketService(handler);
    }
}
