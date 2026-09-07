package com.shiyu.ai.model.adapter.impl;

import com.shiyu.ai.model.adapter.config.PlatformConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericPlatformAdapterTest {
    @Test
    void buildsOpenAiAndDeepSeekClientsAndAppliesDynamicRetryBounds() {
        GenericPlatformAdapter openai = new GenericPlatformAdapter("OPENAI", "http://localhost", "key", "gpt", -3);
        assertTrue(openai.isAvailable());
        assertNotNull(openai.getChatModel(null));
        assertNotNull(openai.getStreamingChatModel("default"));

        PlatformConfig config = new PlatformConfig("OPENAI", "http://localhost", "key", "gpt", .4, 100, 99);
        assertNotNull(openai.createChatModel(config, "gpt-config"));
        assertNotNull(openai.createStreamingChatModel(config, "gpt-config-stream"));
        assertNotNull(openai.createChatModel(null, "gpt-null-config"));

        GenericPlatformAdapter deepseek = new GenericPlatformAdapter("DEEPSEEK", "http://localhost", "key", "deepseek", 20);
        assertNotNull(deepseek.getChatModel("deepseek"));
        assertNotNull(deepseek.getStreamingChatModel("deepseek"));
    }

    @Test
    void returnsNullWhenApiKeyIsMissingAndRejectsMismatchedDynamicPlatform() {
        GenericPlatformAdapter missing = new GenericPlatformAdapter("OPENAI", "http://localhost", " ", "gpt");
        assertTrue(!missing.isAvailable());
        assertNull(missing.getChatModel("gpt"));
        assertNull(missing.getStreamingChatModel("gpt"));

        assertThrows(IllegalStateException.class, () -> missing.createChatModel(
                new PlatformConfig("DEEPSEEK", "http://localhost", "key", "gpt", .2, 10, 1), "gpt"));
        assertNull(missing.createChatModel(
                new PlatformConfig("OPENAI", "http://localhost", " ", "gpt", .2, 10, 1), "gpt"));
        assertNull(missing.createStreamingChatModel(
                new PlatformConfig("OPENAI", "http://localhost", " ", "gpt", .2, 10, 1), "gpt"));
        GenericPlatformAdapter noDefault = new GenericPlatformAdapter("OPENAI", "http://localhost", "key", null);
        assertThrows(IllegalStateException.class, () -> noDefault.getChatModel(null));
        assertThrows(IllegalStateException.class, () -> noDefault.getStreamingChatModel(null));
    }
}
