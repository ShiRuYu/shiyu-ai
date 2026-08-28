package com.shiyu.ai.model.web;

import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.model.vo.AiModelVO;
import com.shiyu.ai.model.vo.AiPlatformVO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.model.adapter.ModelManager;
import com.shiyu.ai.model.api.request.AiModelRequest;
import com.shiyu.ai.model.api.request.AiPlatformRequest;
import com.shiyu.ai.model.api.response.AiModelResponse;
import com.shiyu.ai.model.api.response.AiPlatformResponse;
import com.shiyu.ai.model.service.AiModelService;
import com.shiyu.ai.model.service.AiPlatformService;
import com.shiyu.ai.model.gateway.ModelRouter;
import com.shiyu.ai.model.media.MediaProvider;
import com.shiyu.ai.model.media.MediaProviderRegistry;
import com.shiyu.ai.model.event.ModelCallEvent;
import com.shiyu.ai.model.event.EmbeddingCallEvent;
import com.shiyu.ai.model.chat.ModelStreamEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ModelControllersCoverageTest {
    @Test
    void platformControllerCoversQueriesAndMutations() {
        AiPlatformService service = mock(AiPlatformService.class);
        ModelManager manager = mock(ModelManager.class);
        AiPlatformController controller = new AiPlatformController(service, manager);
        ActorContext actor = new ActorContext(new TenantId(7L), new UserId(9L), false);
        AiPlatformResponse response = new AiPlatformResponse(); response.setId(1L); response.setName("OpenAI");
        when(service.pageResponse(actor, 1, 10, "", "OPENAI")).thenReturn(Pair.of(1L, List.of(response)));
        when(service.enabledResponse(actor)).thenReturn(List.of(response));
        when(service.detailResponse(actor, 1L)).thenReturn(response);
        when(service.codeResponse(actor, "OPENAI")).thenReturn(response);
        when(service.defaultResponse(actor)).thenReturn(response);
        when(service.createResponse(eq(actor), any())).thenReturn(response);
        when(service.updateResponse(eq(actor), eq(1L), any())).thenReturn(response);
        when(service.setDefaultResponse(actor, 1L)).thenReturn(response);
        AiPlatformRequest request = new AiPlatformRequest();
        try (var ignored = mockStatic(ActorContextHttpAdapter.class);
             var mapping = mockStatic(MapstructUtils.class)) {
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            mapping.when(() -> MapstructUtils.convert(anyList(), eq(AiPlatformVO.class))).thenReturn(List.of());
            mapping.when(() -> MapstructUtils.convert(any(AiPlatformResponse.class), eq(AiPlatformVO.class))).thenReturn(null);
            assertTrue(controller.getPage("", "OPENAI", 1, 10).isSuccess());
            assertTrue(controller.getAllEnabled().isSuccess());
            assertTrue(controller.getOptions().isSuccess());
            assertTrue(controller.getById(1L).isSuccess());
            assertTrue(controller.getByCode("OPENAI").isSuccess());
            assertTrue(controller.getDefault().isSuccess());
            assertTrue(controller.create(request).isSuccess());
            assertTrue(controller.update(1L, request).isSuccess());
            assertTrue(controller.delete(1L).isSuccess());
            assertTrue(controller.setDefault(1L).isSuccess());
            assertTrue(controller.reload().isSuccess());
            verify(manager, atLeast(4)).markDirty();
        }
    }

    @Test
    void platformControllerMapsNotFoundAndFailures() {
        AiPlatformService service = mock(AiPlatformService.class);
        ModelManager manager = mock(ModelManager.class);
        AiPlatformController controller = new AiPlatformController(service, manager);
        ActorContext actor = new ActorContext(new TenantId(7L), new UserId(9L), false);
        when(service.detailResponse(actor, 1L)).thenReturn(null);
        when(service.codeResponse(actor, "missing")).thenReturn(null);
        when(service.defaultResponse(actor)).thenReturn(null);
        when(service.createResponse(any(), any())).thenThrow(new IllegalStateException());
        when(service.updateResponse(any(), anyLong(), any())).thenThrow(new IllegalStateException());
        doThrow(new IllegalStateException()).when(service).deleteById(any(), anyLong());
        doThrow(new IllegalStateException()).when(manager).markDirty();
        try (var ignored = mockStatic(ActorContextHttpAdapter.class);
             var mapping = mockStatic(MapstructUtils.class)) {
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            mapping.when(() -> MapstructUtils.convert(any(AiPlatformResponse.class), eq(AiPlatformVO.class))).thenReturn(null);
            assertFalse(controller.getById(1L).isSuccess());
            assertFalse(controller.getByCode("missing").isSuccess());
            assertFalse(controller.getDefault().isSuccess());
            assertFalse(controller.create(new AiPlatformRequest()).isSuccess());
            assertFalse(controller.update(1L, new AiPlatformRequest()).isSuccess());
            assertFalse(controller.delete(1L).isSuccess());
            assertFalse(controller.reload().isSuccess());
        }
    }

    @Test
    void modelControllerCoversQueriesAndMutations() {
        AiModelService service = mock(AiModelService.class);
        ModelManager manager = mock(ModelManager.class);
        AiModelController controller = new AiModelController(service, manager);
        ActorContext actor = new ActorContext(new TenantId(7L), new UserId(9L), false);
        AiModelResponse response = new AiModelResponse(); response.setId(1L); response.setModelName("gpt");
        when(service.pageResponse(actor, 2L, 1, 10)).thenReturn(Pair.of(1L, List.of(response)));
        when(service.byPlatformResponse(actor, 2L)).thenReturn(List.of(response));
        when(service.byPlatformCodeResponse(actor, "OPENAI")).thenReturn(List.of(response));
        when(service.detailResponse(actor, 1L)).thenReturn(response);
        when(service.defaultResponse(actor, 2L)).thenReturn(response);
        when(service.createResponse(eq(actor), any())).thenReturn(response);
        when(service.updateResponse(eq(actor), eq(1L), any())).thenReturn(response);
        when(service.setDefaultResponse(actor, 1L)).thenReturn(response);
        AiModelRequest request = new AiModelRequest();
        try (var ignored = mockStatic(ActorContextHttpAdapter.class);
             var mapping = mockStatic(MapstructUtils.class)) {
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            mapping.when(() -> MapstructUtils.convert(anyList(), eq(AiModelVO.class))).thenReturn(List.of());
            mapping.when(() -> MapstructUtils.convert(any(AiModelResponse.class), eq(AiModelVO.class))).thenReturn(null);
            assertTrue(controller.getPage(2L, 1, 10).isSuccess());
            assertTrue(controller.getByPlatformId(2L).isSuccess());
            assertTrue(controller.getByPlatformCode("OPENAI").isSuccess());
            assertTrue(controller.getOptions(2L).isSuccess());
            assertTrue(controller.getById(1L).isSuccess());
            assertTrue(controller.getDefaultByPlatformId(2L).isSuccess());
            assertTrue(controller.create(request).isSuccess());
            assertTrue(controller.update(1L, request).isSuccess());
            assertTrue(controller.delete(1L).isSuccess());
            assertTrue(controller.deleteBatch(List.of(1L, 2L)).isSuccess());
            assertTrue(controller.setDefault(1L).isSuccess());
            verify(manager, atLeast(5)).markDirty();
        }
    }

    @Test
    void modelControllerMapsNotFoundAndFailures() {
        AiModelService service = mock(AiModelService.class);
        ModelManager manager = mock(ModelManager.class);
        AiModelController controller = new AiModelController(service, manager);
        ActorContext actor = new ActorContext(new TenantId(7L), new UserId(9L), false);
        when(service.detailResponse(actor, 1L)).thenReturn(null);
        when(service.defaultResponse(actor, 2L)).thenReturn(null);
        when(service.createResponse(any(), any())).thenThrow(new IllegalStateException());
        when(service.updateResponse(any(), anyLong(), any())).thenThrow(new IllegalStateException());
        doThrow(new IllegalStateException()).when(service).deleteById(any(), anyLong());
        doThrow(new IllegalStateException()).when(service).deleteByIds(any(), anyList());
        doThrow(new IllegalStateException()).when(service).setDefaultResponse(any(), anyLong());
        try (var ignored = mockStatic(ActorContextHttpAdapter.class);
             var mapping = mockStatic(MapstructUtils.class)) {
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            mapping.when(() -> MapstructUtils.convert(any(AiModelResponse.class), eq(AiModelVO.class))).thenReturn(null);
            assertFalse(controller.getById(1L).isSuccess());
            assertFalse(controller.getDefaultByPlatformId(2L).isSuccess());
            assertFalse(controller.create(new AiModelRequest()).isSuccess());
            assertFalse(controller.update(1L, new AiModelRequest()).isSuccess());
            assertFalse(controller.delete(1L).isSuccess());
            assertFalse(controller.deleteBatch(List.of(1L)).isSuccess());
            assertFalse(controller.setDefault(1L).isSuccess());
        }
    }

    @Test
    void gatewayAndMediaControllersMapTenantRoutesAndProviderOperations() {
        ModelRouter router = new ModelRouter();
        ModelGatewayController gateway = new ModelGatewayController(router);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            assertTrue(gateway.models().isSuccess());
            assertTrue(gateway.routes().isSuccess());
            ModelGatewayController.RouteRequest request = new ModelGatewayController.RouteRequest();
            request.setName("primary");
            request.setOrderedModels(List.of("configured"));
            var saved = gateway.save(request).getData();
            assertNotNull(saved);
            assertTrue(gateway.test(saved.id(), null).isSuccess());
            assertTrue(gateway.health("default", "configured").isSuccess());
            assertTrue(gateway.healthById("default:configured").isSuccess());
            assertThrows(org.springframework.web.server.ResponseStatusException.class,
                    () -> gateway.healthById("malformed"));
        }

        MediaProvider provider = mock(MediaProvider.class);
        when(provider.textToSpeech(anyString(), any(), any())).thenReturn(new byte[] {1, 2});
        when(provider.translate(anyString(), any(), any())).thenReturn("translated");
        when(provider.understandImage(any(), any(), any())).thenReturn(new MediaProvider.VisionResult("seen", List.of("label")));
        when(provider.generateImage(anyString(), any())).thenReturn(new MediaProvider.ImageResult("key", "image/png", 10, 20));
        MediaController media = new MediaController(new MediaProviderRegistry(List.of(provider)));
        MediaController.TtsRequest tts = new MediaController.TtsRequest(); tts.text = "hello";
        assertTrue(media.tts(tts).isSuccess());
        MediaController.TranslateRequest translate = new MediaController.TranslateRequest(); translate.text = "hello";
        assertTrue(media.translate(translate).isSuccess());
        MediaController.ImageRequest image = new MediaController.ImageRequest(); image.imageBase64 = "aGk=";
        assertTrue(media.understand(image).isSuccess());
        MediaController.GenerateRequest generate = new MediaController.GenerateRequest(); generate.prompt = "cat";
        assertTrue(media.generate(generate).isSuccess());
    }

    @Test
    void preservesModelEventAndStreamDefaults() {
        ModelCallEvent call = new ModelCallEvent("OPENAI", "gpt", 3, 2, 10, "run", " ", new TenantId(7L), new UserId(8L));
        assertEquals(5, call.getTotalTokens());
        assertEquals(new TenantId(7L), call.getTenantId());
        assertNotNull(call.getSourceId());
        EmbeddingCallEvent embedding = new EmbeddingCallEvent("bge", 5, 2, 1, 3, "source", new TenantId(7L), new UserId(8L));
        assertEquals("source", embedding.getSourceId());
        assertEquals(1, embedding.getVectorCount());

        ModelStreamEvent stream = new ModelStreamEvent(ModelStreamEvent.Type.TEXT_DELTA, 0,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertEquals("", stream.text());
        assertTrue(stream.toolCalls().isEmpty());
        assertEquals("hello", ModelStreamEvent.text(1, "hello", "provider").text());
        ModelStreamEvent populated = new ModelStreamEvent(ModelStreamEvent.Type.TEXT_DELTA, 0,
                "text", "reasoning", null, null, "{}", null, null, null,
                null, null, null, null, "provider", List.of());
        assertEquals("reasoning", populated.reasoning());
        assertEquals("{}", populated.toolArguments());
        assertThrows(IllegalArgumentException.class, () -> new ModelStreamEvent(null, 0,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null));
    }
}
