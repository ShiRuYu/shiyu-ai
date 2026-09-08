package com.shiyu.ai.knowledge.point.impl;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.knowledge.graph.KnowledgeGraph;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.service.KnowledgeDocumentRelationService;
import com.shiyu.ai.knowledge.service.KnowledgeRelationService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import com.shiyu.ai.knowledge.domain.model.KnowledgeBO;
import com.shiyu.ai.knowledge.point.KnowledgePointService;
import com.shiyu.ai.common.core.api.PageData;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class KnowledgePointServiceImplTenantTest {

    @Test
    void scopesPointLookupToActorTenant() {
        KnowledgeRepository repository = mock(KnowledgeRepository.class);
        KnowledgePointServiceImpl service = service(repository);
        ActorContext actor = new ActorContext(new TenantId(17L), new UserId(2L), false);

        assertThrows(ServiceException.class, () -> service.get(actor, 41L));

        verify(repository).findById(new TenantId(17L), 41L);
    }

    @Test
    void rejectsMissingActorBeforePointRepositoryAccess() {
        KnowledgeRepository repository = mock(KnowledgeRepository.class);
        KnowledgePointServiceImpl service = service(repository);

        assertThrows(ServiceException.class, () -> service.get(null, 41L));

        verifyNoInteractions(repository);
    }

    @Test
    void createsUpdatesDeletesAndPagesPointsWithinSpace() {
        KnowledgeRepository repository = mock(KnowledgeRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeGraph graph = mock(KnowledgeGraph.class);
        KnowledgeRelationService relations = mock(KnowledgeRelationService.class);
        KnowledgeDocumentRelationService documents = mock(KnowledgeDocumentRelationService.class);
        KnowledgePointServiceImpl service = new KnowledgePointServiceImpl(repository, spaces, graph, relations, documents);
        ActorContext actor = new ActorContext(new TenantId(17L), new UserId(2L), false);
        KnowledgeBO point = new KnowledgeBO(); point.setId(41L); point.setTenantId(17L); point.setSpaceId(5L); point.setCode("math"); point.setName("Math"); point.setDifficultyLevel(2);
        when(repository.existsBySpaceAndCode(actor.tenantId(), 5L, "math")).thenReturn(false);
        when(repository.insert(eq(actor.tenantId()), any(KnowledgeBO.class))).thenReturn(1);
        when(repository.update(actor.tenantId(), point)).thenReturn(1);
        when(repository.deleteByIdAndSpace(actor.tenantId(), 41L, 5L)).thenReturn(1);
        when(repository.findById(actor.tenantId(), 41L)).thenReturn(point);
        when(repository.pageBySpace(actor.tenantId(), 5L, 1, 100, null, null)).thenReturn(new PageData<>(List.of(point), 1));
        assertEquals("math", service.create(actor, 5L, new KnowledgePointService.CreatePointRequest("math", "Math", null, 2, "ALG", "tag")).code());
        assertEquals(1, service.page(actor, 5L, 1, 200, null, null).getItems().size());
        assertEquals("Updated", service.update(actor, 41L, new KnowledgePointService.UpdatePointRequest("Updated", null, 3, null, null)).name());
        service.delete(actor, 41L);
        verify(graph).addNode(eq(actor.tenantId()), any()); verify(graph).removeNode(actor.tenantId(), 41L);
        verify(relations).removeAllRelations(actor, 41L); verify(documents).replaceDocuments(actor, 41L, List.of());
    }

    @Test
    void failsUpdateWhenTenantScopedWriteAffectsNoRows() {
        KnowledgeRepository repository = mock(KnowledgeRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeBO point = new KnowledgeBO();
        point.setId(41L);
        point.setTenantId(17L);
        point.setSpaceId(5L);
        point.setName("Math");
        when(repository.findById(new TenantId(17L), 41L)).thenReturn(point);
        when(repository.update(eq(new TenantId(17L)), any(KnowledgeBO.class))).thenReturn(0);

        KnowledgePointServiceImpl service = new KnowledgePointServiceImpl(
                repository, spaces, mock(KnowledgeGraph.class),
                mock(KnowledgeRelationService.class), mock(KnowledgeDocumentRelationService.class));
        ActorContext actor = new ActorContext(new TenantId(17L), new UserId(2L), false);

        assertThrows(ServiceException.class, () -> service.update(actor, 41L,
                new KnowledgePointService.UpdatePointRequest("Updated", null, null, null, null)));
    }

    private KnowledgePointServiceImpl service(KnowledgeRepository repository) {
        return new KnowledgePointServiceImpl(
                repository,
                mock(KnowledgeSpaceService.class),
                mock(KnowledgeGraph.class),
                mock(KnowledgeRelationService.class),
                mock(KnowledgeDocumentRelationService.class));
    }
}
