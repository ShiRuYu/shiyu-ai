package com.shiyu.ai.knowledge.rag;

import com.shiyu.ai.knowledge.port.repository.KnowledgeChunkRepository;
import com.shiyu.ai.model.embedding.EmbeddingService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DocumentIngestionServiceTest {

    @Test
    void rejectsIngestionWithoutAnExplicitActor() {
        DocumentIngestionService service = new DocumentIngestionService(
                mock(EmbeddingService.class), mock(KnowledgeChunkRepository.class), List.of());

        assertThrows(IllegalArgumentException.class,
                () -> service.ingest((ActorContext) null, 1L, 2L, 3L, "content", List.of()));
    }

    @Test
    void actorIngestionPassesExplicitOwnerToEmbedding() {
        EmbeddingService embedding = mock(EmbeddingService.class);
        when(embedding.embed(any(ActorContext.class), eq("content"))).thenReturn(new float[]{1F, 2F});
        DocumentIngestionService service = new DocumentIngestionService(
                embedding, mock(KnowledgeChunkRepository.class), List.of());
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);

        service.ingest(actor, 1L, 2L, 3L, "content", List.of());

        verify(embedding).embed(actor, "content");
    }
}
