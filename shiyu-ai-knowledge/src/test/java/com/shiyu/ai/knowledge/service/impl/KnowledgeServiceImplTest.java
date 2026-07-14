package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeDO;
import com.shiyu.ai.dal.repository.knowledge.KnowledgeRepository;
import com.shiyu.ai.knowledge.dto.CreateKnowledgeRequest;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.dto.UpdateKnowledgeRequest;
import com.shiyu.ai.knowledge.graph.KnowledgeGraph;
import com.shiyu.ai.knowledge.search.KnowledgeSearchService;
import com.shiyu.ai.knowledge.service.DocumentKnowledgeService;
import com.shiyu.ai.knowledge.service.KnowledgeRelationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
class KnowledgeServiceImplTest {

    @Mock
    private KnowledgeRepository knowledgeRepository;
    @Mock
    private KnowledgeGraph knowledgeGraph;
    @Mock
    private KnowledgeSearchService knowledgeSearchService;
    @Mock
    private KnowledgeRelationService knowledgeRelationService;
    @Mock
    private DocumentKnowledgeService documentKnowledgeService;

    private KnowledgeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new KnowledgeServiceImpl(knowledgeRepository, knowledgeGraph,
                knowledgeSearchService, knowledgeRelationService, documentKnowledgeService);
    }

    @Test
    void testGetById() {
        KnowledgeDO mockDO = new KnowledgeDO();
        mockDO.setId(1L);
        mockDO.setCode("MATH-01");
        mockDO.setName("微积分");
        when(knowledgeRepository.findById(1L)).thenReturn(mockDO);

        KnowledgeResponse result = service.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("微积分", result.name());
    }

    @Test
    void testGetByIdNotFound() {
        when(knowledgeRepository.findById(999L)).thenReturn(null);
        assertThrows(Exception.class, () -> service.getById(999L));
    }

    @Test
    void testCreate() {
        when(knowledgeRepository.existsByCode("NEW-01")).thenReturn(false);
        doAnswer(invocation -> {
            KnowledgeDO k = invocation.getArgument(0);
            k.setId(100L);
            return null;
        }).when(knowledgeRepository).insert(any(KnowledgeDO.class));

        CreateKnowledgeRequest request = new CreateKnowledgeRequest("NEW-01", "新知识", "描述", 2, "math", "tag1");
        KnowledgeResponse result = service.create(request);

        assertNotNull(result);
        verify(knowledgeSearchService).indexKnowledge(any(KnowledgeDO.class));
        verify(knowledgeGraph).addNode(any());
    }

    @Test
    void testCreateDuplicateCode() {
        when(knowledgeRepository.existsByCode("EXIST")).thenReturn(true);
        CreateKnowledgeRequest request = new CreateKnowledgeRequest("EXIST", "已存在", null, 1, null, null);
        assertThrows(Exception.class, () -> service.create(request));
    }

    @Test
    void testDelete() {
        KnowledgeDO mockDO = new KnowledgeDO();
        mockDO.setId(1L);
        when(knowledgeRepository.findById(1L)).thenReturn(mockDO);

        service.delete(1L);

        verify(knowledgeSearchService).removeFromIndex(1L);
        verify(knowledgeGraph).removeNode(1L);
        verify(knowledgeRelationService).removeAllRelations(1L);
        verify(documentKnowledgeService).deleteByKnowledgeId(1L);
        verify(knowledgeRepository).deleteById(1L);
    }

    @Test
    void testUpdate() {
        KnowledgeDO mockDO = new KnowledgeDO();
        mockDO.setId(1L);
        mockDO.setName("旧名称");
        when(knowledgeRepository.findById(1L)).thenReturn(mockDO);

        UpdateKnowledgeRequest request = new UpdateKnowledgeRequest("新名称", "新描述", 3, "science", "tag2");
        service.update(1L, request);

        assertEquals("新名称", mockDO.getName());
        assertEquals("新描述", mockDO.getDescription());
        assertEquals(3, mockDO.getDifficulty());
        assertEquals("science", mockDO.getCategory());
        verify(knowledgeRepository).update(mockDO);
        verify(knowledgeSearchService).indexKnowledge(mockDO);
    }
}
