package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.knowledge.domain.RelationType;
import com.shiyu.ai.knowledge.domain.model.KnowledgeBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeRelationBO;
import com.shiyu.ai.knowledge.graph.KnowledgeGraph;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRelationRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class KnowledgeRelationServiceImplTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(11), new UserId(12), false);

    @Test
    void listsDirectionalRelationsAndFiltersMissingTargets() {
        KnowledgeRelationRepository relations = mock(KnowledgeRelationRepository.class); KnowledgeRepository knowledge = mock(KnowledgeRepository.class);
        KnowledgeGraph graph = mock(KnowledgeGraph.class); KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeBO source = knowledge(1L, 7L, "source"); KnowledgeBO target = knowledge(2L, 7L, "target"); KnowledgeRelationBO pre = relation(1L, 2L, "PRE");
        when(knowledge.findById(ACTOR.tenantId(), 1L)).thenReturn(source); when(knowledge.findById(ACTOR.tenantId(), 2L)).thenReturn(target); when(knowledge.findById(ACTOR.tenantId(), 9L)).thenReturn(null);
        when(relations.findBySourceId(ACTOR.tenantId(), 7L, 1L)).thenReturn(List.of(pre)); when(relations.findByTargetId(ACTOR.tenantId(), 7L, 1L)).thenReturn(List.of(pre));
        when(relations.findBySourceIdAndType(ACTOR.tenantId(), 7L, 1L, "PRE")).thenReturn(List.of(pre)); when(relations.findByTargetIdAndType(ACTOR.tenantId(), 7L, 1L, "PRE")).thenReturn(List.of(relation(9L, 1L, "PRE")));
        when(relations.findBySourceIdAndType(ACTOR.tenantId(), 7L, 1L, "RELATED")).thenReturn(List.of(relation(1L, 2L, "RELATED")));
        KnowledgeRelationServiceImpl service = new KnowledgeRelationServiceImpl(relations, knowledge, graph, spaces);
        assertEquals(1, service.list(ACTOR, 1L).size()); assertEquals(1, service.getPrerequisites(ACTOR, 1L).size()); assertTrue(service.getSubsequent(ACTOR, 1L).isEmpty()); assertEquals(1, service.getRelated(ACTOR, 1L).size());
        verify(spaces, atLeast(4)).requireAccess(eq(7L), eq(KnowledgeSpaceService.SpaceRole.VIEWER), eq(ACTOR));
    }

    @Test
    void addsRejectsCyclesAndRemovesRelationsWithGraphEdges() {
        KnowledgeRelationRepository relations = mock(KnowledgeRelationRepository.class); KnowledgeRepository knowledge = mock(KnowledgeRepository.class); KnowledgeGraph graph = mock(KnowledgeGraph.class); KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeBO source = knowledge(1L, 7L, "source"); KnowledgeBO target = knowledge(2L, 7L, "target"); when(knowledge.findById(ACTOR.tenantId(), 1L)).thenReturn(source); when(knowledge.findById(ACTOR.tenantId(), 2L)).thenReturn(target); when(relations.exists(any(), anyLong(), anyLong(), anyLong(), anyString())).thenReturn(false); when(graph.findPath(ACTOR.tenantId(), 2L, 1L)).thenReturn(List.of());
        KnowledgeRelationServiceImpl service = new KnowledgeRelationServiceImpl(relations, knowledge, graph, spaces);
        when(relations.insert(eq(ACTOR.tenantId()), any(KnowledgeRelationBO.class))).thenReturn(1);
        service.addRelation(ACTOR, 1L, 2L, RelationType.PRE, null); verify(relations).insert(eq(ACTOR.tenantId()), any()); verify(graph).addEdge(eq(ACTOR.tenantId()), eq(1L), eq(2L), eq("PRE"), eq(1.0));
        when(relations.exists(any(), anyLong(), anyLong(), anyLong(), anyString())).thenReturn(true); assertThrows(ServiceException.class, () -> service.addRelation(ACTOR, 1L, 2L, RelationType.RELATED, 0.5));
        assertThrows(ServiceException.class, () -> service.addRelation(ACTOR, 1L, 1L, RelationType.PRE, 1.0));
        when(relations.exists(any(), anyLong(), anyLong(), anyLong(), anyString())).thenReturn(false); when(graph.findPath(ACTOR.tenantId(), 2L, 1L)).thenReturn(List.of(2L, 1L)); assertThrows(ServiceException.class, () -> service.addRelation(ACTOR, 1L, 2L, RelationType.PRE, 1.0));
        when(knowledge.findById(ACTOR.tenantId(), 2L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> service.addRelation(ACTOR, 1L, 2L, RelationType.RELATED, 1.0));
        when(knowledge.findById(ACTOR.tenantId(), 2L)).thenReturn(knowledge(2L, 8L, "foreign"));
        assertThrows(ServiceException.class, () -> service.addRelation(ACTOR, 1L, 2L, RelationType.RELATED, 1.0));
        assertThrows(ServiceException.class, () -> service.list(null, 1L));
        when(knowledge.findById(ACTOR.tenantId(), 99L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> service.list(ACTOR, 99L));
        when(relations.findBySourceId(ACTOR.tenantId(), 7L, 1L)).thenReturn(List.of(relation(1L, 2L, "PRE"))); when(relations.findByTargetId(ACTOR.tenantId(), 7L, 1L)).thenReturn(List.of(relation(2L, 1L, "RELATED")));
        service.removeRelation(ACTOR, 1L, 2L, RelationType.PRE); service.removeAllRelations(ACTOR, 1L); verify(relations).deleteBySourceIdOrTargetId(ACTOR.tenantId(), 7L, 1L); verify(graph, atLeast(2)).removeEdge(any(), anyLong(), anyLong(), anyString());
    }

    @Test
    void failsWhenRelationInsertAffectsNoRows() {
        KnowledgeRelationRepository relations = mock(KnowledgeRelationRepository.class);
        KnowledgeRepository knowledge = mock(KnowledgeRepository.class);
        KnowledgeGraph graph = mock(KnowledgeGraph.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        when(knowledge.findById(ACTOR.tenantId(), 1L)).thenReturn(knowledge(1L, 7L, "source"));
        when(knowledge.findById(ACTOR.tenantId(), 2L)).thenReturn(knowledge(2L, 7L, "target"));
        when(relations.exists(any(), anyLong(), anyLong(), anyLong(), anyString())).thenReturn(false);
        when(graph.findPath(ACTOR.tenantId(), 2L, 1L)).thenReturn(List.of());
        when(relations.insert(eq(ACTOR.tenantId()), any(KnowledgeRelationBO.class))).thenReturn(0);

        KnowledgeRelationServiceImpl service = new KnowledgeRelationServiceImpl(relations, knowledge, graph, spaces);

        assertThrows(ServiceException.class, () -> service.addRelation(ACTOR, 1L, 2L, RelationType.PRE, null));
        verify(graph).findPath(ACTOR.tenantId(), 2L, 1L);
        verify(graph, never()).addEdge(any(), anyLong(), anyLong(), anyString(), anyDouble());
    }

    private static KnowledgeBO knowledge(Long id, Long space, String name) { KnowledgeBO value = new KnowledgeBO(); value.setId(id); value.setSpaceId(space); value.setCode(name); value.setName(name); return value; }
    private static KnowledgeRelationBO relation(Long source, Long target, String type) { KnowledgeRelationBO value = new KnowledgeRelationBO(); value.setSourceId(source); value.setTargetId(target); value.setRelationType(type); value.setWeight(1D); return value; }
}
