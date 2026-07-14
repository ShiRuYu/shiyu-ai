package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.knowledge.rag.RagOrchestrator;
import com.shiyu.ai.knowledge.rag.RagOrchestrator.RagChunk;
import com.shiyu.ai.knowledge.rag.RagOrchestrator.RagResult;
import com.shiyu.ai.knowledge.rag.RagService;
import com.shiyu.ai.knowledge.rag.RagService.RagRetrievalResult;
import com.shiyu.ai.knowledge.rag.RagServiceImpl;
import com.shiyu.ai.knowledge.search.KnowledgeSearchService;
import com.shiyu.ai.knowledge.search.SearchResult;
import com.shiyu.ai.knowledge.search.SearchSource;
import com.shiyu.ai.knowledge.service.DocumentKnowledgeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
class RagServiceImplTest {

    @Mock
    private KnowledgeSearchService knowledgeSearchService;
    @Mock
    private DocumentKnowledgeService documentKnowledgeService;
    @Mock
    private RagOrchestrator ragOrchestrator;

    private RagServiceImpl ragService;

    @BeforeEach
    void setUp() {
        ragService = new RagServiceImpl(knowledgeSearchService, documentKnowledgeService, ragOrchestrator);
    }

    @Test
    void testRetrieveFromKnowledgeBase() {
        when(knowledgeSearchService.search("微积分", 5))
                .thenReturn(List.of(new SearchResult(1L, "微积分", "MATH-01", "math", 0.95f)));

        RagRetrievalResult result = ragService.retrieve("微积分", SearchSource.KNOWLEDGE, 5);

        assertTrue(result.success());
        assertEquals(1, result.documents().size());
        assertEquals("微积分", result.documents().get(0).content().split(": ")[0]);
    }

    @Test
    void testRetrieveFromDocument() {
        RagChunk chunk = new RagChunk("微积分基本定理", 0.9, Map.of("chunkIndex", "3"));
        when(ragOrchestrator.retrieve("微积分", 5))
                .thenReturn(new RagResult(List.of(chunk), "图上下文"));

        RagRetrievalResult result = ragService.retrieve("微积分", SearchSource.DOCUMENT, 5);

        assertTrue(result.success());
        assertEquals(2, result.documents().size());
        assertEquals("微积分基本定理", result.documents().get(0).content());
    }

    @Test
    void testRetrieveEmptyQuery() {
        RagRetrievalResult result = ragService.retrieve("", SearchSource.KNOWLEDGE, 5);
        assertFalse(result.success());
        assertNotNull(result.errorMessage());
    }

    @Test
    void testRetrieveDefault() {
        when(ragOrchestrator.retrieve("线性代数", 5))
                .thenReturn(new RagResult(List.of(), ""));

        RagRetrievalResult result = ragService.retrieve("线性代数");
        assertTrue(result.success());
    }
}
