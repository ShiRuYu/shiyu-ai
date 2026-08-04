package com.shiyu.ai.usage.config;

import com.shiyu.ai.usage.port.repository.UsageRecordRepository;
import com.shiyu.ai.usage.collector.UsageCollector;
import com.shiyu.ai.usage.realtime.UsageRealtimePublisher;
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
                                         @Autowired(required = false) UsageRealtimePublisher publisher) {
        UsageCollector collector = new UsageCollector(usageRecordRepository);
        if (publisher != null) {
            collector.setRealtimePublisher(publisher);
            log.info("WebSocket 推送服务已接入 UsageCollector");
        }
        log.info("创建 UsageCollector");
        return collector;
    }
}
