package com.shiyu.ai.model.adapter;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.model.adapter.config.PlatformConfig;
import com.shiyu.ai.model.config.PlatformProperties;
import com.shiyu.ai.model.domain.model.AiModelBO;
import com.shiyu.ai.model.domain.model.AiPlatformBO;
import com.shiyu.ai.model.port.repository.AiModelRepository;
import com.shiyu.ai.model.port.repository.AiPlatformRepository;
import com.shiyu.ai.model.adapter.impl.OllamaPlatformAdapter;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ModelManagerTest {
    @Test
    void loadsDatabaseAdaptersAndRoutesByModel() {
        AiPlatformRepository platforms = mock(AiPlatformRepository.class); AiModelRepository models = mock(AiModelRepository.class);
        PlatformProperties props = new PlatformProperties(); props.setTenantId(9L); props.getOpenai().setApiKey("key"); props.init();
        AiPlatformBO platform = new AiPlatformBO(); platform.setId(1L); platform.setCode("OPENAI"); platform.setName("OpenAI"); platform.setBaseUrl("http://openai");
        AiModelBO model = new AiModelBO(); model.setModelName("gpt-test");
        when(platforms.selectAllEnabled(new TenantId(9L))).thenReturn(List.of(platform)); when(models.selectDefaultByPlatformId(new TenantId(9L), 1L)).thenReturn(model);
        when(platforms.selectDefault(new TenantId(9L))).thenReturn(platform);
        ModelManager manager = new ModelManager(platforms, models, props);
        manager.reloadFromDb();
        assertTrue(manager.isDbLoaded()); assertEquals("OPENAI", manager.getDefaultPlatform()); assertEquals("gpt-test", manager.getDefaultModelName("OPENAI"));
        assertEquals("OPENAI", manager.resolvePlatform("gpt-test")); assertTrue(manager.getAvailablePlatforms().contains("OPENAI"));
        assertTrue(manager.availableModels().stream().anyMatch(x -> x.platform().equals("OPENAI")));
        PlatformConfig config = new PlatformConfig("OPENAI", "http://openai", "key", "gpt-test", .7, 10, 1);
        assertNotNull(manager.getChatModel(config)); assertNotNull(manager.getStreamingChatModel(config));
        manager.markDirty(); assertFalse(manager.isDbLoaded()); assertNotNull(manager.getAdapter("OPENAI"));
        manager.refreshCache("OPENAI"); manager.refreshAllCache(); manager.unregisterAdapter("OPENAI"); assertFalse(manager.isPlatformAvailable("OPENAI"));
        assertThrows(IllegalArgumentException.class, () -> manager.getAdapter("MISSING"));
    }

    @Test
    void selectsAdapterByConfiguredAdapterTypeInsteadOfPlatformCode() {
        AiPlatformRepository platforms = mock(AiPlatformRepository.class);
        AiModelRepository models = mock(AiModelRepository.class);
        PlatformProperties props = new PlatformProperties();
        props.setTenantId(9L);
        props.init();

        AiPlatformBO platform = new AiPlatformBO();
        platform.setId(7L);
        platform.setCode("LOCAL_OLLAMA");
        platform.setName("本地 Ollama");
        platform.setBaseUrl("http://localhost:11434");
        platform.setAdapterType("OLLAMA");
        AiModelBO model = new AiModelBO();
        model.setModelName("llama3");

        when(platforms.selectAllEnabled(new TenantId(9L))).thenReturn(List.of(platform));
        when(models.selectDefaultByPlatformId(new TenantId(9L), 7L)).thenReturn(model);
        when(platforms.selectDefault(new TenantId(9L))).thenReturn(platform);

        ModelManager manager = new ModelManager(platforms, models, props);
        manager.reloadFromDb();

        assertInstanceOf(OllamaPlatformAdapter.class, manager.getAdapter("LOCAL_OLLAMA"));
    }

    @Test
    void skipsMalformedPlatformWithoutDiscardingValidDatabaseAdapters() {
        AiPlatformRepository platforms = mock(AiPlatformRepository.class);
        AiModelRepository models = mock(AiModelRepository.class);
        PlatformProperties props = new PlatformProperties();
        props.setTenantId(9L);
        props.init();

        AiPlatformBO valid = new AiPlatformBO();
        valid.setId(8L);
        valid.setCode("VALID");
        valid.setName("有效平台");
        valid.setBaseUrl("http://valid");
        valid.setAdapterType("OPENAI_COMPATIBLE");
        AiPlatformBO malformed = new AiPlatformBO();
        malformed.setId(9L);
        malformed.setCode("BROKEN");
        malformed.setName("错误平台");
        malformed.setBaseUrl("http://broken");
        malformed.setAdapterType("UNKNOWN");

        when(platforms.selectAllEnabled(new TenantId(9L))).thenReturn(List.of(valid, malformed));
        when(platforms.selectDefault(new TenantId(9L))).thenReturn(valid);

        ModelManager manager = new ModelManager(platforms, models, props);
        manager.reloadFromDb();

        assertTrue(manager.isDbLoaded());
        assertNotNull(manager.getAdapter("VALID"));
        assertThrows(IllegalArgumentException.class, () -> manager.getAdapter("BROKEN"));
    }

    @Test
    void selectsAdapterByAdapterTypeForStandaloneConfiguration() {
        ModelManager manager = new ModelManager(mock(AiPlatformRepository.class), mock(AiModelRepository.class), new PlatformProperties());
        PlatformConfig config = new PlatformConfig("LOCAL", "OLLAMA", "http://localhost:11434", null, "llama3", .7, 128, 1);

        assertNotNull(manager.getChatModel(config));
    }

    @Test
    void doesNotReuseOllamaAdapterForExplicitOpenAiCompatibleConfiguration() {
        ModelManager manager = new ModelManager(mock(AiPlatformRepository.class), mock(AiModelRepository.class), new PlatformProperties());
        manager.registerAdapter(new OllamaPlatformAdapter("http://localhost:11434", "llama3", .7, 1));
        PlatformConfig config = new PlatformConfig("OLLAMA", "OPENAI_COMPATIBLE", "http://openai", "key", "gpt-test", .7, 128, 1);

        assertNotNull(manager.getChatModel(config));
    }

    @Test
    void fallsBackToDefaultsWhenDatabaseUnavailableAndSupportsRegistration() {
        AiPlatformRepository platforms = mock(AiPlatformRepository.class); AiModelRepository models = mock(AiModelRepository.class);
        PlatformProperties props = new PlatformProperties(); props.setTenantId(null);
        ModelManager manager = new ModelManager(platforms, models, props);
        manager.reloadFromDb();
        assertFalse(manager.isDbLoaded()); assertTrue(List.of("OPENAI", "DEEPSEEK", "OPENROUTER", "SILICON_FLOW", "OLLAMA").contains(manager.getDefaultPlatform()));
        ModelAdapter adapter = mock(ModelAdapter.class); when(adapter.getPlatformType()).thenReturn("CUSTOM"); when(adapter.isAvailable()).thenReturn(true); when(adapter.getDefaultModelName()).thenReturn("custom-model");
        manager.registerAdapter(adapter); assertTrue(manager.isPlatformAvailable("CUSTOM")); assertEquals("CUSTOM", manager.resolvePlatform("custom-model"));
        manager.unregisterAdapter("CUSTOM"); verify(adapter).clearCache();
        assertThrows(IllegalArgumentException.class, () -> manager.getChatModel((PlatformConfig) null));
    }
}
