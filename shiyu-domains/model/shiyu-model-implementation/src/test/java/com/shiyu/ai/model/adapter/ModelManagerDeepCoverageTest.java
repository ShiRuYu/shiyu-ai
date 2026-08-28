package com.shiyu.ai.model.adapter;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.model.adapter.config.PlatformConfig;
import com.shiyu.ai.model.config.PlatformProperties;
import com.shiyu.ai.model.domain.model.AiModelBO;
import com.shiyu.ai.model.domain.model.AiPlatformBO;
import com.shiyu.ai.model.port.repository.AiModelRepository;
import com.shiyu.ai.model.port.repository.AiPlatformRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ModelManagerDeepCoverageTest {
    @Test
    void loadsGenericAndOllamaDatabaseAdaptersWithFallbackFields() {
        AiPlatformRepository platforms = mock(AiPlatformRepository.class);
        AiModelRepository models = mock(AiModelRepository.class);
        PlatformProperties properties = new PlatformProperties();
        properties.setTenantId(3L);
        properties.getOpenai().setApiKey("external-openai");
        properties.init();
        AiPlatformBO openai = platform(1L, "OPENAI", "http://openai");
        AiPlatformBO ollama = platform(2L, "OLLAMA", "http://ollama");
        AiModelBO model = new AiModelBO(); model.setModelName("gpt-db");
        when(platforms.selectAllEnabled(new TenantId(3L))).thenReturn(List.of(openai, ollama));
        when(models.selectDefaultByPlatformId(new TenantId(3L), 1L)).thenReturn(model);
        when(models.selectDefaultByPlatformId(new TenantId(3L), 2L)).thenThrow(new IllegalStateException("db"));
        when(platforms.selectDefault(new TenantId(3L))).thenReturn(openai);

        ModelManager manager = new ModelManager(platforms, models, properties);
        manager.reloadFromDb();
        assertTrue(manager.isDbLoaded());
        assertEquals("OPENAI", manager.defaultPlatform());
        assertEquals("gpt-db", manager.getDefaultModelName("OPENAI"));
        assertTrue(manager.isPlatformAvailable("OPENAI"));
        assertFalse(manager.getAvailablePlatforms().isEmpty());
        assertEquals("OPENAI", manager.resolvePlatform("gpt-db"));
        assertEquals("OPENAI", manager.resolvePlatform(" "));
        assertNull(manager.getDefaultModelName("MISSING"));

        PlatformConfig config = new PlatformConfig("OPENAI", "http://override", "key", "configured", .2, 10, 1);
        assertTrue(manager.getChatModel(config, "gpt-db") != null);
        assertTrue(manager.getStreamingChatModel(config, null) != null);
        manager.refreshCache("MISSING");
    }

    @Test
    void handlesEmptyDatabaseAndEmptyAdapterFallbacks() {
        AiPlatformRepository platforms = mock(AiPlatformRepository.class);
        AiModelRepository models = mock(AiModelRepository.class);
        PlatformProperties properties = new PlatformProperties();
        properties.setTenantId(4L);
        when(platforms.selectAllEnabled(new TenantId(4L))).thenReturn(List.of());
        ModelManager manager = new ModelManager(platforms, models, properties);
        manager.reloadFromDb();
        assertFalse(manager.isDbLoaded());
        assertTrue(List.of("OPENAI", "DEEPSEEK", "OPENROUTER", "SILICON_FLOW", "OLLAMA")
                .contains(manager.getDefaultPlatform()));
        assertTrue(manager.availableModels().size() >= 5);

        PlatformProperties noTenant = new PlatformProperties();
        ModelManager empty = new ModelManager(platforms, models, noTenant);
        assertEquals("SILICON_FLOW", empty.getDefaultPlatform());
        assertThrows(IllegalArgumentException.class, () -> empty.getAdapter("missing"));
    }

    @Test
    void refreshesAndReplacesRegisteredAdapters() {
        ModelManager manager = new ModelManager(mock(AiPlatformRepository.class), mock(AiModelRepository.class), new PlatformProperties());
        ModelAdapter adapter = mock(ModelAdapter.class);
        when(adapter.getPlatformType()).thenReturn("CUSTOM");
        when(adapter.getDefaultModelName()).thenReturn("");
        when(adapter.isAvailable()).thenReturn(false);
        manager.registerAdapter(adapter);
        assertEquals("CUSTOM", manager.getAllAdapters().get("CUSTOM").getPlatformType());
        assertTrue(manager.availableModels().stream().anyMatch(value -> "CUSTOM".equals(value.id())));
        assertFalse(manager.isPlatformAvailable("CUSTOM"));
        manager.refreshAllCache();
        manager.unregisterAdapter("CUSTOM");
        assertFalse(manager.isPlatformAvailable("CUSTOM"));
    }

    @Test
    void validatesConfigOverloadsAndLazyCacheLifecycle() {
        AiPlatformRepository platforms = mock(AiPlatformRepository.class);
        AiModelRepository models = mock(AiModelRepository.class);
        PlatformProperties properties = new PlatformProperties();
        ModelManager manager = new ModelManager(platforms, models, properties);
        assertThrows(IllegalArgumentException.class, () -> manager.getChatModel((PlatformConfig) null));
        assertThrows(IllegalArgumentException.class, () -> manager.getStreamingChatModel((PlatformConfig) null));
        assertThrows(IllegalArgumentException.class, () -> manager.getChatModel("UNKNOWN", "model"));
        assertThrows(IllegalArgumentException.class, () -> manager.getStreamingChatModel("UNKNOWN", "model"));
        manager.markDirty();
        assertFalse(manager.isDbLoaded());
        assertTrue(manager.getAllAdapters().isEmpty() || manager.getAllAdapters().size() >= 5);
        manager.refreshAllCache();
    }

    @Test
    void fallsBackWhenDatabaseLoadFailsAndCoversOptionalConfigValues() {
        AiPlatformRepository platforms = mock(AiPlatformRepository.class);
        AiModelRepository models = mock(AiModelRepository.class);
        PlatformProperties properties = new PlatformProperties();
        properties.setTenantId(9L);
        properties.getDeepseek().setBaseUrl("http://deepseek");
        properties.getDeepseek().setModel("deep-model");
        when(platforms.selectAllEnabled(new TenantId(9L))).thenThrow(new IllegalStateException("database down"));
        when(platforms.selectDefault(new TenantId(9L))).thenThrow(new IllegalStateException("database down"));

        ModelManager manager = new ModelManager(platforms, models, properties);
        manager.reloadFromDb();

        assertFalse(manager.isDbLoaded());
        assertTrue(manager.getAllAdapters().containsKey("DEEPSEEK"));
        assertEquals("DEEPSEEK", manager.resolvePlatform("deep-model"));
        manager.refreshCache("DEEPSEEK");
        manager.unregisterAdapter("MISSING");
    }

    @Test
    void handlesNullPlatformListAndConfigModelFallback() {
        AiPlatformRepository platforms = mock(AiPlatformRepository.class);
        AiModelRepository models = mock(AiModelRepository.class);
        PlatformProperties properties = new PlatformProperties();
        properties.setTenantId(10L);
        when(platforms.selectAllEnabled(new TenantId(10L))).thenReturn(null);
        ModelManager manager = new ModelManager(platforms, models, properties);
        manager.reloadFromDb();
        PlatformConfig config = new PlatformConfig("OPENAI", null, null, "gpt-4o", null, null, null);
        assertNull(manager.getChatModel(config));
        assertNull(manager.getStreamingChatModel(config));
    }

    private AiPlatformBO platform(long id, String code, String url) {
        AiPlatformBO value = new AiPlatformBO();
        value.setId(id); value.setCode(code); value.setName(code); value.setBaseUrl(url);
        return value;
    }
}
