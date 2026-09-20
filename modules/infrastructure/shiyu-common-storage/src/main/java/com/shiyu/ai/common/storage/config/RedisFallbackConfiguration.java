package com.shiyu.ai.common.storage.config;

import com.shiyu.ai.common.storage.api.IdempotencyStore;
import com.shiyu.ai.common.storage.idempotency.LocalIdempotencyStore;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 定义 Redis Fallback 基础设施或应用能力的配置项及装配规则。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = "shiyu.infrastructure.redis",
        name = "provider",
        havingValue = "disabled",
        matchIfMissing = true)
public class RedisFallbackConfiguration {

    /**
     * 执行 Redis Fallback 相关业务数据，并返回处理结果。
     *
     * @return 返回 Redis Fallback 相关操作生成的结果数据。
     */
    @Bean
    public IdempotencyStore localIdempotencyStore() {
        return new LocalIdempotencyStore();
    }
}
