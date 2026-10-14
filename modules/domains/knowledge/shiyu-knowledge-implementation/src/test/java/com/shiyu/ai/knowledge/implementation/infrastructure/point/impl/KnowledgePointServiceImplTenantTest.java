package com.shiyu.ai.knowledge.implementation.infrastructure.point.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.exception.ServiceException;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeDocumentRelationService;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeRelationService;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService;
import com.shiyu.ai.knowledge.implementation.application.service.graph.KnowledgeGraph;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeSpaceBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.implementation.infrastructure.point.KnowledgePointService;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 验证 知识 Point Service Impl 租户 相关功能、边界条件、异常路径和协作行为。
 */
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
        KnowledgePointServiceImpl service =
                new KnowledgePointServiceImpl(mock(KnowledgeEnterpriseRepository.class), repository, spaces, graph, relations, documents);
        ActorContext actor = new ActorContext(new TenantId(17L), new UserId(2L), false);
        KnowledgeBO point = new KnowledgeBO();
        point.setId(41L);
        point.setTenantId(17L);
        point.setSpaceId(5L);
        point.setCode("math");
        point.setName("Math");
        point.setDifficultyLevel(2);
        when(repository.existsBySpaceAndCode(actor.tenantId(), 5L, "math")).thenReturn(false);
        when(repository.insert(eq(actor.tenantId()), any(KnowledgeBO.class))).thenReturn(1);
        when(repository.update(actor.tenantId(), point)).thenReturn(1);
        when(repository.deleteByIdAndSpace(actor.tenantId(), 41L, 5L)).thenReturn(1);
        when(repository.findById(actor.tenantId(), 41L)).thenReturn(point);
        when(repository.pageBySpace(actor.tenantId(), 5L, 1, 100, null, null))
                .thenReturn(new PageData<>(List.of(point), 1));
        assertEquals(
                "math",
                service.create(
                                actor,
                                5L,
                                new KnowledgePointService.CreatePointRequest(
                                        "math", "Math", null, 2, "ALG", "tag"))
                        .code());
        assertEquals(1, service.page(actor, 5L, 1, 200, null, null).getItems().size());
        assertEquals(
                "Updated",
                service.update(
                                actor,
                                41L,
                                new KnowledgePointService.UpdatePointRequest(
                                        "Updated", null, 3, null, null))
                        .name());
        service.delete(actor, 41L);
        verify(graph).addNode(eq(actor.tenantId()), any());
        verify(graph).removeNode(actor.tenantId(), 41L);
        verify(relations).removeAllRelations(actor, 41L);
        verify(documents).replaceDocuments(actor, 41L, List.of());
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

        KnowledgePointServiceImpl service =
                new KnowledgePointServiceImpl(
                        mock(KnowledgeEnterpriseRepository.class),
                        repository,
                        spaces,
                        mock(KnowledgeGraph.class),
                        mock(KnowledgeRelationService.class),
                        mock(KnowledgeDocumentRelationService.class));
        ActorContext actor = new ActorContext(new TenantId(17L), new UserId(2L), false);

        assertThrows(
                ServiceException.class,
                () ->
                        service.update(
                                actor,
                                41L,
                                new KnowledgePointService.UpdatePointRequest(
                                        "Updated", null, null, null, null)));
    }

    private KnowledgePointServiceImpl service(KnowledgeRepository repository) {
        return new KnowledgePointServiceImpl(
                mock(KnowledgeEnterpriseRepository.class),
                repository,
                mock(KnowledgeSpaceService.class),
                mock(KnowledgeGraph.class),
                mock(KnowledgeRelationService.class),
                mock(KnowledgeDocumentRelationService.class));
    }

    @Test
    void studentCatalogOnlyIncludesTenantVisibleEducationSpaces() {
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeRepository repository = mock(KnowledgeRepository.class);
        KnowledgePointServiceImpl service = new KnowledgePointServiceImpl(
                enterprise, repository, mock(KnowledgeSpaceService.class),
                mock(KnowledgeGraph.class), mock(KnowledgeRelationService.class),
                mock(KnowledgeDocumentRelationService.class));
        ActorContext actor = new ActorContext(new TenantId(17L), new UserId(2L), false);
        KnowledgeSpaceBO visible = space(5L, "EDUCATION", "TENANT");
        KnowledgeSpaceBO privateSpace = space(6L, "EDUCATION", "PRIVATE");
        KnowledgeSpaceBO otherDomain = space(7L, "GENERAL", "TENANT");
        when(enterprise.findActiveSpacesByTenant(actor.tenantId()))
                .thenReturn(List.of(visible, privateSpace, otherDomain));
        when(repository.findAll(actor.tenantId()))
                .thenReturn(List.of(point(41L, 5L), point(42L, 6L), point(43L, 7L)));

        assertEquals(List.of(41L), service.listStudentEducationPoints(actor).stream()
                .map(response -> response.id()).toList());
        assertEquals(41L, service.getStudentEducationPoint(actor, 41L).id());
        assertThrows(ServiceException.class, () -> service.getStudentEducationPoint(actor, 42L));
        assertThrows(ServiceException.class, () -> service.getStudentEducationPoint(actor, 43L));
    }

    private static KnowledgeSpaceBO space(Long id, String domain, String access) {
        KnowledgeSpaceBO space = new KnowledgeSpaceBO();
        space.setId(id);
        space.setDomainCode(domain);
        space.setAccessMode(access);
        return space;
    }

    private static KnowledgeBO point(Long id, Long spaceId) {
        KnowledgeBO point = new KnowledgeBO();
        point.setId(id);
        point.setSpaceId(spaceId);
        point.setName("Math " + id);
        return point;
    }
}
