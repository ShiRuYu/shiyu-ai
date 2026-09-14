package com.shiyu.ai.governance.implementation.usage.config;

import com.shiyu.ai.governance.contract.UsageGovernance;
import com.shiyu.ai.governance.implementation.usage.port.BillingPriceProvider;
import com.shiyu.ai.governance.implementation.usage.port.repository.UsageRecordRepository;
import com.shiyu.ai.governance.implementation.usage.realtime.UsageRealtimePublisher;
import com.shiyu.ai.governance.implementation.usage.service.UsageRecordService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@code UsageAutoConfiguration} 提供治理模块的配置项，并集中声明其默认值和运行约束。
 */
@Slf4j
@Configuration
public class UsageAutoConfiguration {

    /**
     * {@code usageRecordService} 执行当前类型定义的业务操作。
     *
     * @param usageRecordRepository 参数值，用于执行当前操作。
     * @param publisher 参数值，用于执行当前操作。
     * @param billingPriceProvider 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    @ConditionalOnMissingBean
    public UsageRecordService usageRecordService(
            UsageRecordRepository usageRecordRepository,
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

    /**
     * {@code usageGovernance} 执行当前类型定义的业务操作。
     *
     * @param usageRecordService 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    @ConditionalOnMissingBean(UsageGovernance.class)
    public UsageGovernance usageGovernance(UsageRecordService usageRecordService) {
        return usageRecordService;
    }
}
