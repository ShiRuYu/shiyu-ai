package com.shiyu.ai.model.service;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.model.api.request.AiModelRequest;
import com.shiyu.ai.model.api.request.AiPlatformRequest;
import com.shiyu.ai.model.api.response.AiModelResponse;
import com.shiyu.ai.model.api.response.AiPlatformResponse;
import com.shiyu.ai.model.config.PlatformProperties;
import com.shiyu.ai.model.domain.model.AiModelBO;
import com.shiyu.ai.model.domain.model.AiPlatformBO;
import com.shiyu.ai.model.port.repository.AiModelRepository;
import com.shiyu.ai.model.port.repository.AiPlatformRepository;
import com.shiyu.ai.model.service.impl.AiModelServiceImpl;
import com.shiyu.ai.model.service.impl.AiPlatformServiceImpl;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import com.shiyu.ai.common.core.utils.MapstructUtils;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ModelCatalogServiceTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(81), new UserId(82), false);

    @Test
    void platformPropertiesRejectsMissingCodeAndResolvesDefaults() {
        PlatformProperties properties = new PlatformProperties();
        properties.init();

        assertNull(properties.getDefaultModel(null));
        assertEquals("gpt-4o-mini", properties.getDefaultModel("openai"));
    }

    @Test
    void platformServiceScopesQueriesAndHandlesDefaultsAndApiKeys() throws Exception {
        AiPlatformRepository repository = mock(AiPlatformRepository.class);
        PlatformProperties properties = new PlatformProperties();
        properties.getDeepseek().setApiKey("secret");
        AiPlatformServiceImpl service = inject(new AiPlatformServiceImpl(), "aiPlatformRepository", repository);
        inject(service, "platformProperties", properties);

        AiPlatformBO platform = new AiPlatformBO();
        platform.setId(1L); platform.setName("DeepSeek"); platform.setCode("DEEPSEEK"); platform.setIsDefault("Y");
        AiPlatformResponse response = new AiPlatformResponse(); response.setCode("DEEPSEEK");
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(AiPlatformBO.class), eq(AiPlatformResponse.class))).thenReturn(response);
            mapper.when(() -> MapstructUtils.convert(any(AiPlatformRequest.class), eq(AiPlatformBO.class))).thenReturn(platform);
        when(repository.selectPage(ACTOR.tenantId(), 1, 10, "deep", "deepseek")).thenReturn(Pair.of(1L, List.of(platform)));
        when(repository.selectAllEnabled(ACTOR.tenantId())).thenReturn(List.of(platform));
        when(repository.selectById(ACTOR.tenantId(), 1L)).thenReturn(platform);
        when(repository.selectByCode(ACTOR.tenantId(), "DEEPSEEK")).thenReturn(platform);
        when(repository.selectDefault(ACTOR.tenantId())).thenReturn(platform);
        when(repository.create(eq(ACTOR.tenantId()), any())).thenReturn(platform);
        when(repository.update(eq(ACTOR.tenantId()), any())).thenReturn(platform);

        assertEquals("DEEPSEEK", service.pageResponse(ACTOR, 1, 10, "deep", "deepseek").getRight().get(0).getCode());
        assertEquals(1, service.enabledResponse(ACTOR).size());
        assertNotNull(service.detailResponse(ACTOR, 1L));
        assertNotNull(service.codeResponse(ACTOR, "DEEPSEEK"));
        assertNotNull(service.defaultResponse(ACTOR));

        AiPlatformRequest request = new AiPlatformRequest();
        request.setName("DeepSeek"); request.setCode("DEEPSEEK"); request.setIsDefault("Y");
        assertNotNull(service.createResponse(ACTOR, request));
        assertNotNull(service.updateResponse(ACTOR, 1L, request));
        assertNotNull(service.setDefaultResponse(ACTOR, 1L));
        service.deleteById(ACTOR, 1L);
        service.getOptions(ACTOR);
        verify(repository, atLeastOnce()).clearDefaultExcept(eq(ACTOR.tenantId()), anyLong());
        when(repository.selectById(ACTOR.tenantId(), 99L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> service.setDefaultResponse(ACTOR, 99L));
        }
    }

    @Test
    void modelServiceFillsPlatformAndClearsDefaultOnCreateAndUpdate() throws Exception {
        AiModelRepository repository = mock(AiModelRepository.class);
        AiPlatformRepository platforms = mock(AiPlatformRepository.class);
        AiModelServiceImpl service = inject(new AiModelServiceImpl(), "aiModelRepository", repository);
        inject(service, "aiPlatformRepository", platforms);
        AiPlatformBO platform = new AiPlatformBO(); platform.setId(2L); platform.setName("OpenAI");
        AiModelBO model = new AiModelBO(); model.setId(3L); model.setPlatformId(2L); model.setModelName("gpt"); model.setIsDefault("Y");
        AiModelResponse response = new AiModelResponse(); response.setPlatformName("OpenAI");
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
        mapper.when(() -> MapstructUtils.convert(any(AiModelBO.class), eq(AiModelResponse.class))).thenReturn(response);
        mapper.when(() -> MapstructUtils.convert(any(AiModelRequest.class), eq(AiModelBO.class))).thenReturn(model);
        when(platforms.selectById(ACTOR.tenantId(), 2L)).thenReturn(platform);
        when(repository.selectPage(ACTOR.tenantId(), 2L, 1, 10)).thenReturn(Pair.of(1L, List.of(model)));
        when(repository.selectByPlatformId(ACTOR.tenantId(), 2L)).thenReturn(List.of(model));
        when(platforms.selectByCode(ACTOR.tenantId(), "OPENAI")).thenReturn(platform);
        when(repository.selectById(ACTOR.tenantId(), 3L)).thenReturn(model);
        when(repository.selectDefaultByPlatformId(ACTOR.tenantId(), 2L)).thenReturn(model);
        when(repository.create(eq(ACTOR.tenantId()), any())).thenReturn(model);
        when(repository.update(eq(ACTOR.tenantId()), any())).thenReturn(model);

        assertEquals("OpenAI", service.pageResponse(ACTOR, 2L, 1, 10).getRight().get(0).getPlatformName());
        assertEquals(1, service.byPlatformResponse(ACTOR, 2L).size());
        assertEquals(1, service.byPlatformCodeResponse(ACTOR, "OPENAI").size());
        assertNotNull(service.detailResponse(ACTOR, 3L));
        assertNotNull(service.defaultResponse(ACTOR, 2L));

        AiModelRequest request = new AiModelRequest(); request.setPlatformId(2L); request.setModelName("gpt"); request.setIsDefault("Y");
        assertNotNull(service.createResponse(ACTOR, request));
        assertNotNull(service.updateResponse(ACTOR, 3L, request));
        assertNotNull(service.setDefaultResponse(ACTOR, 3L));
        service.deleteById(ACTOR, 3L); service.deleteByIds(ACTOR, List.of(3L)); service.getOptions(ACTOR, 2L);
        verify(repository, atLeastOnce()).clearDefaultExcept(eq(ACTOR.tenantId()), eq(2L), any());
        when(platforms.selectByCode(ACTOR.tenantId(), "MISSING")).thenReturn(null);
        assertTrue(service.byPlatformCodeResponse(ACTOR, "MISSING").isEmpty());
        }
    }

    private static <T> T inject(T target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
        return target;
    }
}
