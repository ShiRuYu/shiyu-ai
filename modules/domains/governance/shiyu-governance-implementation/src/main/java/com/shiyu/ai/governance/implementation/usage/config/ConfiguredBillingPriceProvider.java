package com.shiyu.ai.governance.implementation.usage.config;

import com.shiyu.ai.governance.implementation.usage.port.BillingPriceProvider;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** 根据应用显式配置查询模型单价，未配置时返回未知费用。 */
public final class ConfiguredBillingPriceProvider implements BillingPriceProvider {
    private final Map<String, PriceSnapshot> prices;

    /**
     * 加载模型价格配置，并校验模型标识和非负单价。
     *
     * @param properties 应用中的模型价格配置。
     */
    public ConfiguredBillingPriceProvider(BillingPricingProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        prices = new HashMap<>();
        List<BillingPricingProperties.ModelPrice> models = properties.getModels();
        if (models == null) return;

        for (BillingPricingProperties.ModelPrice modelPrice : models) {
            String platform = requireText(modelPrice.getPlatform(), "platform");
            String model = requireText(modelPrice.getModel(), "model");
            BigDecimal promptPerToken =
                    requireNonNegative(modelPrice.getPromptPerToken(), "promptPerToken");
            BigDecimal completionPerToken =
                    requireNonNegative(modelPrice.getCompletionPerToken(), "completionPerToken");
            String version =
                    modelPrice.getVersion() == null || modelPrice.getVersion().isBlank()
                            ? "configured"
                            : modelPrice.getVersion();
            PriceSnapshot snapshot =
                    new PriceSnapshot(
                            platform, model, promptPerToken, completionPerToken, version);
            if (prices.putIfAbsent(key(platform, model), snapshot) != null) {
                throw new IllegalArgumentException(
                        "duplicate model price for " + platform + ":" + model);
            }
        }
    }

    /**
     * 按平台忽略大小写、按模型名称精确匹配价格。
     *
     * @param platform 模型服务平台。
     * @param model 模型标识。
     * @return 已配置的价格；未配置时返回 {@code null}。
     */
    @Override
    public PriceSnapshot price(String platform, String model) {
        if (platform == null || model == null) return null;
        return prices.get(key(platform, model));
    }

    private static String key(String platform, String model) {
        return platform.trim().toUpperCase(Locale.ROOT) + ":" + model.trim();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }

    private static BigDecimal requireNonNegative(BigDecimal value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.signum() < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return value;
    }
}
