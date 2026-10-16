package com.shiyu.ai.governance.implementation.usage.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.math.BigDecimal;

import com.shiyu.ai.governance.implementation.usage.config.UsageAutoConfiguration;
import com.shiyu.ai.governance.implementation.usage.port.BillingPriceProvider;
import com.shiyu.ai.governance.implementation.usage.port.repository.UsageRecordRepository;

/** 验证模型价格配置的读取、匹配和校验。 */
class ConfiguredBillingPriceProviderTest {
    @Test
    void resolvesConfiguredPricesByPlatformAndModel() {
        BillingPricingProperties properties = new BillingPricingProperties();
        BillingPricingProperties.ModelPrice model = new BillingPricingProperties.ModelPrice();
        model.setPlatform("DeepSeek");
        model.setModel("deepseek-chat");
        model.setPromptPerToken(new BigDecimal("0.000001"));
        model.setCompletionPerToken(new BigDecimal("0.000002"));
        model.setVersion("price-2026-10");
        properties.getModels().add(model);

        ConfiguredBillingPriceProvider provider = new ConfiguredBillingPriceProvider(properties);

        var price = provider.price("DEEPSEEK", "deepseek-chat");
        assertEquals(new BigDecimal("0.000001"), price.promptPerToken());
        assertEquals("price-2026-10", price.version());
        assertNull(provider.price("DEEPSEEK", "unconfigured-model"));
    }

    @Test
    void rejectsNegativeConfiguredTokenPrices() {
        BillingPricingProperties properties = new BillingPricingProperties();
        BillingPricingProperties.ModelPrice model = new BillingPricingProperties.ModelPrice();
        model.setPlatform("DEEPSEEK");
        model.setModel("deepseek-chat");
        model.setPromptPerToken(new BigDecimal("-0.1"));
        model.setCompletionPerToken(BigDecimal.ZERO);
        properties.getModels().add(model);

        assertThrows(
                IllegalArgumentException.class,
                () -> new ConfiguredBillingPriceProvider(properties));
    }

    @Test
    void bindsConfiguredModelPricesIntoTheDefaultProviderBean() {
        new ApplicationContextRunner()
                .withUserConfiguration(UsageAutoConfiguration.class)
                .withBean(UsageRecordRepository.class, () -> mock(UsageRecordRepository.class))
                .withPropertyValues(
                        "shiyu.governance.usage.pricing.models[0].platform=DEEPSEEK",
                        "shiyu.governance.usage.pricing.models[0].model=deepseek-chat",
                        "shiyu.governance.usage.pricing.models[0].prompt-per-token=0.000001",
                        "shiyu.governance.usage.pricing.models[0].completion-per-token=0.000002",
                        "shiyu.governance.usage.pricing.models[0].version=price-v1")
                .run(
                        context -> {
                            assertThat(context).hasNotFailed();
                            BillingPriceProvider provider =
                                    context.getBean(BillingPriceProvider.class);
                            var price = provider.price("DEEPSEEK", "deepseek-chat");
                            assertEquals(new BigDecimal("0.000001"), price.promptPerToken());
                            assertEquals("price-v1", price.version());
                        });
    }
}
