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
 * 定义 用量 Auto 基础设施或应用能力的配置项及装配规则。
 */
@Slf4j
@Configuration
public class UsageAutoConfiguration {

    /**
     * 执行 用量 Auto 相关业务数据，并返回处理结果。
     *
     * @param usageRecordRepository 用于完成本次业务处理的 usageRecordRepository 参数。
     * @param publisher 用于完成本次业务处理的 publisher 参数。
     * @param billingPriceProvider 用于完成本次业务处理的 billingPriceProvider 参数。
     * @return 返回 用量 Auto 相关操作生成的结果数据。
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
     * 执行 用量 Auto 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Bean
    @ConditionalOnMissingBean(UsageGovernance.class)
    public UsageGovernance usageGovernance(UsageRecordService usageRecordService) {
        return usageRecordService;
    }
}
