package com.shiyu.ai.model.gateway;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ModelProviderCapabilitiesTest {
    @Test
    void appliesDefaultsAndFeatureFlags() {
        ModelProviderCapabilities defaults = new ModelProviderCapabilities("OPENAI", "gpt", null, 0);
        assertEquals(Set.of("chat"), defaults.features());
        assertEquals(8192, defaults.contextWindow());
        assertEquals(4096, defaults.maxOutputTokens());
        assertTrue(defaults.streaming());
        assertFalse(defaults.supports(null));
        assertFalse(defaults.supports("tools"));

        ModelProviderCapabilities features = new ModelProviderCapabilities(
                "OPENAI", "gpt", Set.of("tool_calls", "parallel_tool_calls", "structured"),
                2048, false, true, true, true, true, List.of("high"), 128,
                false, true, false);
        assertTrue(features.supports("tool_calls"));
        assertTrue(features.tools());
        assertTrue(features.parallelTools());
        assertEquals(List.of("high"), features.reasoningLevels());
        assertEquals(128, features.maxOutputTokens());
    }

    @Test
    void rejectsMissingProviderOrModelAndNormalizesInvalidLimits() {
        assertThrows(IllegalArgumentException.class,
                () -> new ModelProviderCapabilities(" ", "gpt", Set.of(), 1));
        assertThrows(IllegalArgumentException.class,
                () -> new ModelProviderCapabilities("OPENAI", null, Set.of(), 1));
        ModelProviderCapabilities normalized = new ModelProviderCapabilities(
                "OPENAI", "gpt", Set.of("chat"), -1, true, false, false, false,
                false, null, 0, true, false, true);
        assertEquals(8192, normalized.contextWindow());
        assertEquals(List.of(), normalized.reasoningLevels());
        assertEquals(4096, normalized.maxOutputTokens());
    }

    @Test
    void handlesBlankFeaturesAndCopiesMutableInputs() {
        Set<String> features = new java.util.HashSet<>(Set.of("chat"));
        List<String> reasoning = new java.util.ArrayList<>(List.of("low"));
        ModelProviderCapabilities capabilities = new ModelProviderCapabilities(
                "OPENAI", "gpt", features, 1, true, false, false, false,
                false, reasoning, 1, true, false, true);
        features.add("tools");
        reasoning.add("high");
        assertFalse(capabilities.supports("tools"));
        assertEquals(List.of("low"), capabilities.reasoningLevels());
        assertFalse(capabilities.supports(""));
        assertEquals(1, capabilities.contextWindow());
        assertEquals(1, capabilities.maxOutputTokens());
    }
}
