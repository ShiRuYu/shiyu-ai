package com.shiyu.ai.governance.implementation.usage.port;

import java.math.BigDecimal;
import java.util.Objects;

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
     * 以 USD/token 为单位保存某个模型的一组输入、输出单价及其版本。
     */
    record PriceSnapshot(
            String platform,
            String model,
            BigDecimal promptPerToken,
            BigDecimal completionPerToken,
            String version) {
        public PriceSnapshot {
            Objects.requireNonNull(platform, "platform must not be null");
            Objects.requireNonNull(model, "model must not be null");
            Objects.requireNonNull(promptPerToken, "promptPerToken must not be null");
            Objects.requireNonNull(completionPerToken, "completionPerToken must not be null");
            if (promptPerToken.signum() < 0 || completionPerToken.signum() < 0) {
                throw new IllegalArgumentException("token prices must not be negative");
            }
        }
    }
}
