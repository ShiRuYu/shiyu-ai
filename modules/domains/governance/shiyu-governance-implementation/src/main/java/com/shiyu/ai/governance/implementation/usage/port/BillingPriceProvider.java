package com.shiyu.ai.governance.implementation.usage.port;

import java.math.BigDecimal;

/**
 * 创建或提供 Billing Price 相关的业务组件和运行时能力。
 */
public interface BillingPriceProvider {
    /**
     * 执行 Billing Price 相关业务数据，并返回处理结果。
     *
     * @param platform 用于完成本次业务处理的 platform 参数。
     * @param model 用于完成本次业务处理的 model 参数。
     * @return 返回 Billing Price 相关操作生成的结果数据。
     */
    PriceSnapshot price(String platform, String model);

    /**
     * 封装 Price Snapshot 相关的不可变数据及其字段约束。
     */
    record PriceSnapshot(
            String platform,
            String model,
            BigDecimal promptPerToken,
            BigDecimal completionPerToken,
            String version) {}
}
