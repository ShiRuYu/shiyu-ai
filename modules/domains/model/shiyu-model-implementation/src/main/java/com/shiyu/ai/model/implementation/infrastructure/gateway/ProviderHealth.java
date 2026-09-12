package com.shiyu.ai.model.implementation.infrastructure.gateway;

import java.time.Instant;

/**
 * {@code ProviderHealth} 封装模型模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param provider 提供方，表示该记录组件承载的数据。
 * @param model 模型，表示该记录组件承载的数据。
 * @param healthy healthy 属性，表示该记录组件承载的数据。
 * @param consecutiveFailures consecutiveFailures 属性，表示该记录组件承载的数据。
 * @param checkedAt checkedAt 属性，表示该记录组件承载的数据。
 * @param message message 属性，表示该记录组件承载的数据。
 */
public record ProviderHealth(
        String provider,
        String model,
        boolean healthy,
        int consecutiveFailures,
        Instant checkedAt,
        String message) {}
