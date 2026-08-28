package com.shiyu.ai.model.adapter.impl;

import com.shiyu.ai.model.adapter.config.PlatformConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OllamaPlatformAdapterTest {
    @Test
    void createsCachedModelsAndHonorsDefaultsAndDynamicConfiguration() {
        OllamaPlatformAdapter adapter = new OllamaPlatformAdapter("http://127.0.0.1:11434", "llama3", 0.2, 2);

        assertTrue(adapter.isAvailable());
        assertNotNull(adapter.getChatModel(null));
        assertNotNull(adapter.getStreamingChatModel("default"));
        assertNotNull(adapter.createChatModel(null, "llama3"));
        assertNotNull(adapter.createStreamingChatModel(null, "llama3"));

        PlatformConfig config = new PlatformConfig("OLLAMA", "http://127.0.0.1:11435", null,
                "llama3", 0.3, 1024, 1);
        ChatModel dynamic = adapter.createChatModel(config, "llama3");
        StreamingChatModel dynamicStream = adapter.createStreamingChatModel(config, "llama3");
        assertNotNull(dynamic);
        assertNotNull(dynamicStream);
        assertThrows(IllegalStateException.class, () -> adapter.createChatModel(
                new PlatformConfig("OPENAI", "http://localhost", "key", "gpt", 0.2, 10, 1), "gpt"));
        PlatformConfig emptyUrl = new PlatformConfig("OLLAMA", " ", null, "llama3", 0.3, 1024, 1);
        assertNull(adapter.createChatModel(emptyUrl, "llama3"));
        assertNull(adapter.createStreamingChatModel(emptyUrl, "llama3"));
    }

    @Test
    void rejectsUnconfiguredBaseUrlWithoutBuildingProviderClients() {
        OllamaPlatformAdapter adapter = new OllamaPlatformAdapter("  ", null, null, null);

        assertTrue(!adapter.isAvailable());
        assertNull(adapter.getChatModel("llama3"));
        assertNull(adapter.getStreamingChatModel("llama3"));
    }
}
