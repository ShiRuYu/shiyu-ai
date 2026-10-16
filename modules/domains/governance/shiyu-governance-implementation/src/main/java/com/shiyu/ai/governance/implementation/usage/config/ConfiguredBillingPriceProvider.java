package com.shiyu.ai.governance.implementation.usage.config;

import com.shiyu.ai.governance.implementation.usage.port.BillingPriceProvider;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Provides the model prices explicitly configured by the application operator. */
public final class ConfiguredBillingPriceProvider implements BillingPriceProvider {
    private final Map<String, PriceSnapshot> prices;

    /**
     * Loads configured prices and validates their model keys and nonnegative rates.
     *
     * @param properties application model pricing settings.
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
     * Finds the configured price using a case-insensitive platform and exact model name.
     *
     * @param platform provider platform.
     * @param model provider model identifier.
     * @return the configured price, or {@code null} when no price exists.
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
