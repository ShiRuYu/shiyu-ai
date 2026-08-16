package com.shiyu.ai.usage.config;

import com.shiyu.ai.usage.port.repository.UsageRecordRepository;
import com.shiyu.ai.usage.service.UsageRecordService;
import com.shiyu.ai.usage.port.BillingPriceProvider;
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
    public UsageRecordService usageRecordService(UsageRecordRepository usageRecordRepository,
                                         @Autowired(required = false) UsageRealtimePublisher publisher,
                                         @Autowired(required = false) BillingPriceProvider billingPriceProvider) {
        UsageRecordService service = new UsageRecordService(usageRecordRepository);
        if (publisher != null) {
            service.setRealtimePublisher(publisher);
            log.info("WebSocket 推送服务已接入 UsageRecordService");
        }
        if (billingPriceProvider != null) service.setBillingPriceProvider(billingPriceProvider);
        log.info("创建 UsageRecordService");
        return service;
    }
}
