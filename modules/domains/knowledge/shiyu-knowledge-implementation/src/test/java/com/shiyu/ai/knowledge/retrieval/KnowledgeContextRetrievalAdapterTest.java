package com.shiyu.ai.knowledge.retrieval;

import com.shiyu.ai.runtime.ContextQuery;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KnowledgeContextRetrievalAdapterTest {
    @Test
    void filtersNamespacesParsesSpaceIdsAndMapsCitations() {
        KnowledgeRetrievalService retrieval = mock(KnowledgeRetrievalService.class);
        KnowledgeContextRetrievalAdapter adapter = new KnowledgeContextRetrievalAdapter(retrieval);
        ContextQuery ignored = new ContextQuery(new TenantId(7), new UserId(9), "memory", "question", 3, Map.of());
        assertTrue(adapter.retrieve(ignored).isEmpty());
        when(retrieval.retrieve(any(KnowledgeRetrievalRequest.class)))
                .thenReturn(new KnowledgeRetrievalResult(true, List.of(), List.of(), "", null));
        assertTrue(adapter.retrieve(new ContextQuery(new TenantId(7), new UserId(9), null, "question", 3, Map.of())).isEmpty());

        KnowledgeRetrievalHit hit = new KnowledgeRetrievalHit(2L, 3L, 4L, 5L, 6L,
                "Title", "content", "highlight", 1, "Section", 0.1, 0.2, 0.4, 0.0);
        when(retrieval.retrieve(any(KnowledgeRetrievalRequest.class)))
                .thenReturn(new KnowledgeRetrievalResult(true, List.of(hit), List.of(), "", null));
        ContextQuery query = new ContextQuery(new TenantId(7), new UserId(9), "RAG", "question", 3,
                Map.of("spaceIds", "1, 2"));
        var result = adapter.retrieve(query);
        assertEquals(1, result.size());
        assertEquals("content", result.getFirst().content());
        assertEquals(0.4, result.getFirst().score());
        assertTrue(result.getFirst().relationPath().contains("space:2"));
        verify(retrieval).retrieve(argThat(request -> request.spaceIds().equals(List.of(1L, 2L))
                && request.topK() == 3));

        when(retrieval.retrieve(any(KnowledgeRetrievalRequest.class)))
                .thenReturn(new KnowledgeRetrievalResult(true,
                        List.of(new KnowledgeRetrievalHit(2L, 3L, 4L, 5L, 6L,
                                "Title", "content", null, null, null, 0.1, 0.2, 0.4, 0.7)),
                        List.of(), "", null));
        var reranked = adapter.retrieve(new ContextQuery(new TenantId(7), new UserId(9), "knowledge", "question", 1, Map.of()));
        assertEquals(0.7, reranked.getFirst().score());
    }

    @Test
    void rejectsNonNumericSpaceFilters() {
        KnowledgeRetrievalService retrieval = mock(KnowledgeRetrievalService.class);
        when(retrieval.retrieve(any())).thenReturn(new KnowledgeRetrievalResult(true, List.of(), List.of(), "", null));
        KnowledgeContextRetrievalAdapter adapter = new KnowledgeContextRetrievalAdapter(retrieval);
        ContextQuery query = new ContextQuery(new TenantId(7), new UserId(9), "knowledge", "question", 3,
                Map.of("spaceIds", "1,nope"));
        assertThrows(IllegalArgumentException.class, () -> adapter.retrieve(query));
        ContextQuery blank = new ContextQuery(new TenantId(7), new UserId(9), "knowledge", "question", 3,
                Map.of("spaceIds", " "));
        assertDoesNotThrow(() -> adapter.retrieve(blank));
    }
}
