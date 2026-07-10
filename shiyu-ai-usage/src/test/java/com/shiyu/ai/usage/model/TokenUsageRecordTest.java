package com.shiyu.ai.usage.model;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Tag("dev")
class TokenUsageRecordTest {

    @Test
    void testCreateRecord() {
        TokenUsageRecord record = new TokenUsageRecord(
            "openai", "gpt-4o", 100, 50, 500, 0.01, 1L, "session-1");

        assertNotNull(record.getId());
        assertEquals("openai", record.getPlatform());
        assertEquals("gpt-4o", record.getModel());
        assertEquals(100, record.getPromptTokens());
        assertEquals(50, record.getCompletionTokens());
        assertEquals(150, record.getTotalTokens());
        assertEquals(500, record.getLatencyMs());
        assertEquals(0.01, record.getCost(), 0.001);
        assertEquals(1L, record.getUserId());
        assertEquals("session-1", record.getSessionId());
        assertNotNull(record.getTimestamp());
    }

    @Test
    void testDefaultPricing() {
        ModelPricing pricing = ModelPricing.defaultOpenAI();
        assertEquals("OPENAI", pricing.getPlatform());
        assertEquals("gpt-4o", pricing.getModel());
        assertEquals(0.005, pricing.getInputPricePer1K(), 0.001);
        assertEquals(0.015, pricing.getOutputPricePer1K(), 0.001);
    }

    @Test
    void testCalculateCost() {
        ModelPricing pricing = new ModelPricing("test", "test-model", 1.0, 2.0);
        // 1000 input tokens @ 1.0/1K = 1.0
        // 500 output tokens @ 2.0/1K = 1.0
        // Total = 2.0
        double cost = pricing.calculateCost(1000, 500);
        assertEquals(2.0, cost, 0.001);
    }

    @Test
    void testCalculateCostZeroTokens() {
        ModelPricing pricing = new ModelPricing("test", "test-model", 1.0, 2.0);
        double cost = pricing.calculateCost(0, 0);
        assertEquals(0.0, cost, 0.001);
    }
}
