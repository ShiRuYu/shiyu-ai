package com.shiyu.ai.knowledge.path.impl;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.knowledge.domain.model.KnowledgeBO;
import com.shiyu.ai.knowledge.graph.KnowledgeGraph;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class KnowledgePathServiceImplTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(8), new UserId(4), false);
    private final KnowledgeGraph graph = mock(KnowledgeGraph.class);
    private final KnowledgeRepository repository = mock(KnowledgeRepository.class);
    private final KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
    private final KnowledgePathServiceImpl service = new KnowledgePathServiceImpl(graph, repository, spaces);

    @Test
    void generatesFindsAndFiltersPathsByTenantAndSpace() {
        KnowledgeBO from = point(1L, 10L); KnowledgeBO to = point(2L, 10L);
        when(repository.findById(ACTOR.tenantId(), 1L)).thenReturn(from); when(repository.findById(ACTOR.tenantId(), 2L)).thenReturn(to);
        when(graph.topologicalSort(ACTOR.tenantId(), 2L)).thenReturn(List.of(1L, 2L));
        when(graph.findPath(ACTOR.tenantId(), 1L, 2L)).thenReturn(List.of(1L, 2L));
        when(graph.findMissingPrerequisites(ACTOR.tenantId(), 2L, Set.of(1L))).thenReturn(List.of());
        assertEquals(List.of(1L, 2L), service.generatePath(ACTOR, 2L));
        assertEquals(List.of(1L, 2L), service.findPath(ACTOR, 1L, 2L));
        assertTrue(service.findMissingPrerequisites(ACTOR, 2L, Set.of(1L)).isEmpty());
        verify(spaces, times(4)).requireAccess(10L, KnowledgeSpaceService.SpaceRole.VIEWER, ACTOR);
    }

    @Test
    void rejectsMissingPointsAndCrossSpacePaths() {
        when(repository.findById(ACTOR.tenantId(), 99L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> service.generatePath(ACTOR, 99L));
        when(repository.findById(ACTOR.tenantId(), 1L)).thenReturn(point(1L, 10L)); when(repository.findById(ACTOR.tenantId(), 2L)).thenReturn(point(2L, 11L));
        assertThrows(ServiceException.class, () -> service.findPath(ACTOR, 1L, 2L));
        assertThrows(ServiceException.class, () -> service.findPath(null, 1L, 2L));
    }

    private static KnowledgeBO point(Long id, Long spaceId) { KnowledgeBO point = new KnowledgeBO(); point.setId(id); point.setSpaceId(spaceId); return point; }
}
