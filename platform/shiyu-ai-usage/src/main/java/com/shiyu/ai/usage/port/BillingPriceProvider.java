package com.shiyu.ai.usage.port;

import java.math.BigDecimal;

public interface BillingPriceProvider {
    PriceSnapshot price(String platform, String model);
    record PriceSnapshot(String platform, String model, BigDecimal promptPerToken, BigDecimal completionPerToken, String version) { }
}
