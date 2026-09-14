package com.shiyu.ai.common.storage.config;

import com.shiyu.ai.common.storage.api.IdempotencyStore;
import com.shiyu.ai.common.storage.idempotency.LocalIdempotencyStore;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@code RedisFallbackConfiguration} 提供平台基础设施模块的配置项，并集中声明其默认值和运行约束。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = "shiyu.infrastructure.redis",
        name = "provider",
        havingValue = "disabled",
        matchIfMissing = true)
public class RedisFallbackConfiguration {

    /**
     * {@code localIdempotencyStore} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    public IdempotencyStore localIdempotencyStore() {
        return new LocalIdempotencyStore();
    }
}
