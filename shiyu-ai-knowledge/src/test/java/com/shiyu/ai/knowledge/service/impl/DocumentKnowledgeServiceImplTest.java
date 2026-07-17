package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.dal.repository.knowledge.KnowledgeChunkRepository;
import com.shiyu.ai.dal.repository.knowledge.KnowledgeDocRelationRepository;
import com.shiyu.ai.dal.repository.knowledge.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.rag.DocumentIngestionService;
import com.shiyu.ai.model.embedding.EmbeddingService;
import com.shiyu.ai.vector.VectorStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
class DocumentKnowledgeServiceImplTest {

    @Mock
    private KnowledgeDocumentRepository documentRepository;
    @Mock
    private DocumentIngestionService ingestionService;
    @Mock
    private VectorStore vectorStore;
    @Mock
    private EmbeddingService embeddingService;
    @Mock
    private KnowledgeChunkRepository chunkRepository;
    @Mock
    private KnowledgeDocRelationRepository docRelationRepository;

    private DocumentKnowledgeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DocumentKnowledgeServiceImpl(documentRepository, ingestionService,
                vectorStore, embeddingService, chunkRepository, docRelationRepository);
    }

    @Test
    void testDeleteByKnowledgeId() {
        service.deleteByKnowledgeId(1L);
        verify(docRelationRepository).deleteByKnowledgeId(1L);
    }

    @Test
    void testGetByIdNotFound() {
        assertNull(service.getById(999L));
    }

    @Test
    void testGetByIdFound() {
        var mockDoc = new com.shiyu.ai.dal.dataobject.knowledge.KnowledgeDocumentDO();
        mockDoc.setId(1L);
        mockDoc.setTitle("测试文档");
        mockDoc.setContent("内容");
        mockDoc.setDocType("ARTICLE");
        when(documentRepository.selectById(1L)).thenReturn(mockDoc);

        var result = service.getById(1L);
        assertNotNull(result);
        assertEquals("测试文档", result.title());
    }
}
