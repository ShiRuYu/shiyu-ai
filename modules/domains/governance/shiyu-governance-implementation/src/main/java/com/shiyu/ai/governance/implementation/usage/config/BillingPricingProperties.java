package com.shiyu.ai.governance.implementation.usage.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Optional per-token USD prices used to calculate model usage costs. */
@Getter
@Setter
@ConfigurationProperties(prefix = "shiyu.governance.usage.pricing")
public class BillingPricingProperties {
    private List<ModelPrice> models = new ArrayList<>();

    @Getter
    @Setter
    public static class ModelPrice {
        private String platform;
        private String model;
        private BigDecimal promptPerToken;
        private BigDecimal completionPerToken;
        private String version;
    }
}
