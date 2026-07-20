package com.shiyu.ai.usage.config;

import com.shiyu.ai.dal.agent.repository.UsageRecordRepository;
import com.shiyu.ai.usage.collector.UsageCollector;
import com.shiyu.ai.usage.websocket.UsageWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class UsageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public UsageCollector usageCollector(UsageRecordRepository usageRecordRepository,
                                         @Autowired(required = false) UsageWebSocketService webSocketService) {
        UsageCollector collector = new UsageCollector(usageRecordRepository);
        if (webSocketService != null) {
            collector.setWebSocketService(webSocketService);
            log.info("WebSocket 推送服务已接入 UsageCollector");
        }
        log.info("创建 UsageCollector");
        return collector;
    }
}
