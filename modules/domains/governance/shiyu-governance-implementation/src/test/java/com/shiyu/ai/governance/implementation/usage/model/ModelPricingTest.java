package com.shiyu.ai.governance.implementation.usage.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelPricingTest {
    @Test
    void calculatesTokenCostAndProvidesDefaultOpenAiPricing() {
        ModelPricing pricing = new ModelPricing("x", "m", 0.01, 0.02);
        assertEquals(0.03, pricing.calculateCost(1_000, 1_000), 0.000001);
        assertEquals("OPENAI", ModelPricing.defaultOpenAI().getPlatform());
        assertEquals("gpt-4o", ModelPricing.defaultOpenAI().getModel());
        assertEquals("x", pricing.getPlatform());
        assertEquals("m", pricing.getModel());
        assertEquals(0.01, pricing.getInputPricePer1K());
        assertEquals(0.02, pricing.getOutputPricePer1K());
    }
}
