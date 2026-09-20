package com.shiyu.ai.governance.implementation.web.config;

import com.shiyu.ai.governance.implementation.web.websocket.UsageWebSocketHandler;
import com.shiyu.ai.governance.implementation.web.websocket.UsageWebSocketService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 定义 用量 Web Socket 基础设施或应用能力的配置项及装配规则。
 */
@Configuration
@EnableWebSocket
public class UsageWebSocketConfig implements WebSocketConfigurer {

    /**
     * 创建或保存 用量 Web Socket 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param registry 用于完成本次业务处理的 registry 参数。
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(usageWebSocketHandler(), "/ws/usage").setAllowedOriginPatterns("*");
    }

    /**
     * 执行 用量 Web Socket 相关业务数据，并返回处理结果。
     *
     * @return 返回 用量 Web Socket 相关操作生成的结果数据。
     */
    @Bean
    public UsageWebSocketHandler usageWebSocketHandler() {
        return new UsageWebSocketHandler();
    }

    /**
     * 执行 用量 Web Socket 相关业务数据，并返回处理结果。
     *
     * @param handler 用于完成本次业务处理的 handler 参数。
     * @return 返回 用量 Web Socket 相关操作生成的结果数据。
     */
    @Bean
    public UsageWebSocketService usageWebSocketService(UsageWebSocketHandler handler) {
        return new UsageWebSocketService(handler);
    }
}
