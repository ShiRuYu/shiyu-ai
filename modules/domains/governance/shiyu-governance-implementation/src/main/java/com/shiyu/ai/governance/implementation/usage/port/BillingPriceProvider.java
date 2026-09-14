package com.shiyu.ai.governance.implementation.usage.port;

import java.math.BigDecimal;

/**
 * BillingPriceProvider 边界接口，负责向外部组件提供治理领域相关能力。
 */
public interface BillingPriceProvider {
    /**
     * 执行 {@code price} 定义的接口操作。
     *
     * @param platform 方法参数。
     * @param model 方法参数。
     *
     * @return 操作结果。
     */
    PriceSnapshot price(String platform, String model);

    /**
     * {@code PriceSnapshot} 封装治理模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param platform platform 属性，表示该记录组件承载的数据。
     * @param model 模型，表示该记录组件承载的数据。
     * @param promptPerToken promptPerToken 属性，表示该记录组件承载的数据。
     * @param completionPerToken completionPerToken 属性，表示该记录组件承载的数据。
     * @param version version 属性，表示该记录组件承载的数据。
     */
    record PriceSnapshot(
            String platform,
            String model,
            BigDecimal promptPerToken,
            BigDecimal completionPerToken,
            String version) {}
}
