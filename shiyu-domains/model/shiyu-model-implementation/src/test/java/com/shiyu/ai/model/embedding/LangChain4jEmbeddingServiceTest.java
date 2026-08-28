package com.shiyu.ai.model.embedding;

import com.shiyu.ai.model.embedding.impl.LangChain4jEmbeddingService;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.UserId;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LangChain4jEmbeddingServiceTest {

    @Test
    void localModelIsOptionalInTheBasePackage() {
        LangChain4jEmbeddingService service = new LangChain4jEmbeddingService();

        assertTrue(service.dimension() >= 0);
        if (localModelAvailable()) {
            assertTrue(service.dimension() > 0);
        } else {
            assertEquals(0, service.dimension());
        }
    }

    @Test
    void offlineBgeModelProducesVectorsWhenTheOfflineProfileIsEnabled() {
        assumeTrue(localModelAvailable(), "offline-models profile is not enabled");

        LangChain4jEmbeddingService service = new LangChain4jEmbeddingService();
        float[] vector = service.embed(new TenantId(1), "企业知识平台离线模型验证");

        assertNotNull(vector);
        assertEquals(512, vector.length);
    }

    @Test
    @SuppressWarnings("unchecked")
    void embedsSingleAndBatchRequestsWithActorAttribution() {
        EmbeddingModel model = mock(EmbeddingModel.class);
        ObjectProvider<EmbeddingModel> models = mock(ObjectProvider.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        doReturn(model).when(models).getIfAvailable(any());
        doReturn(3).when(model).dimension();
        doReturn(Response.from(Embedding.from(new float[] {1, 2, 3}))).when(model).embed(any(String.class));
        doReturn(Response.from(List.of(Embedding.from(new float[] {4, 5, 6}), Embedding.from(new float[] {7, 8, 9}))))
                .when(model).embedAll(any());

        LangChain4jEmbeddingService service = new LangChain4jEmbeddingService(models, publisher);
        assertEquals(3, service.dimension());
        assertEquals(3, service.embed(new ActorContext(new TenantId(7), new UserId(8), false), "中文abc").length);
        assertEquals(2, service.embedBatch(new TenantId(7), List.of("中文", "abc")).size());
        verify(publisher, org.mockito.Mockito.times(2)).publishEvent(any(Object.class));
    }

    @Test
    void rejectsMissingTenantActorAndModel() {
        LangChain4jEmbeddingService service = new LangChain4jEmbeddingService();
        assertThrows(IllegalArgumentException.class, () -> service.embed((TenantId) null, "text"));
        assertThrows(IllegalArgumentException.class, () -> service.embed((ActorContext) null, "text"));
        assertThrows(IllegalStateException.class, () -> service.embed(new TenantId(1), "text"));
        assertThrows(IllegalStateException.class, () -> service.embedBatch(new TenantId(1), List.of()));
    }

    private boolean localModelAvailable() {
        try {
            Class.forName("dev.langchain4j.model.embedding.onnx.bgesmallzhv15.BgeSmallZhV15EmbeddingModel");
            return true;
        } catch (ClassNotFoundException | LinkageError exception) {
            return false;
        }
    }
}
