package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.RoleId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.knowledge.port.repository.KnowledgeChunkRepository;
import com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDifficultyScaleBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDifficultyScaleLevelBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceMemberBO;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocRelationRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDifficultyScaleRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRelationRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.service.KnowledgeAuditService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.util.List;

class KnowledgeSpaceServiceImplTenantTest {

    @Test
    void rejectsMissingActorBeforeListingSpaces() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeSpaceServiceImpl service = service(repository);

        assertThrows(ServiceException.class,
                () -> service.page(null, 1, 20, null, null));

        verifyNoInteractions(repository);
    }

    @Test
    void scopesSpacePageQueryToActorTenant() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        when(repository.pageSpacesByTenant(new TenantId(7L), 1, 20, null, null))
                .thenReturn(new PageData<>(java.util.List.of(), 0));
        KnowledgeSpaceServiceImpl service = service(repository);

        service.page(actor(7L), 1, 20, null, null);

        verify(repository).pageSpacesByTenant(new TenantId(7L), 1, 20, null, null);
    }

    @Test
    void hidesSpaceThatDoesNotBelongToActorTenant() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeSpaceServiceImpl service = service(repository);
        ActorContext actor = actor(7L);

        assertThrows(ServiceException.class,
                () -> service.requireAccess(99L, KnowledgeSpaceService.SpaceRole.VIEWER, actor));

        verify(repository).findSpaceByTenant(new TenantId(7L), 99L);
    }

    @Test
    void rejectsMissingActorForAccessibleSpaces() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeSpaceServiceImpl service = service(repository);

        assertThrows(ServiceException.class, () -> service.accessibleSpaces(null));

        verifyNoInteractions(repository);
    }

    @Test
    void createsAndReplacesMembersWithTenantOwnership() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeAuditService audit = mock(KnowledgeAuditService.class);
        KnowledgeSpaceServiceImpl service = service(repository, audit);
        when(repository.findSpaceByTenantAndCode(new TenantId(7L), "new-space")).thenReturn(null);
        when(repository.insertSpace(eq(new TenantId(7L)), any(KnowledgeSpaceBO.class))).thenAnswer(invocation -> {
            KnowledgeSpaceBO space = invocation.getArgument(1);
            space.setId(20L);
            return space;
        });
        var request = new KnowledgeSpaceService.CreateSpaceRequest("new-space", "New Space", null, null,
                null, null, null, null, null, null, null, null, null);
        var view = service.create(actor(7L), request);
        assertEquals(20L, view.id());
        verify(repository).replaceMembers(eq(new TenantId(7L)), eq(20L), any());
        verify(audit).record(any(), eq(20L), eq("SPACE"), eq(20L), eq("CREATE"), eq(request));
    }

    @Test
    void refusesInvalidMemberRolesAndDeletionWithContent() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeRepository knowledge = mock(KnowledgeRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeAuditService audit = mock(KnowledgeAuditService.class);
        KnowledgeSpaceBO space = new KnowledgeSpaceBO(); space.setId(30L); space.setTenantId(7L); space.setCode("custom"); space.setAccessMode("PRIVATE");
        when(repository.findSpaceByTenant(new TenantId(7L), 30L)).thenReturn(space);
        when(repository.hasMember(eq(new TenantId(7L)), eq(30L), eq("USER"), eq(8L), any())).thenReturn(true);
        KnowledgeSpaceServiceImpl service = new KnowledgeSpaceServiceImpl(repository, knowledge,
                mock(KnowledgeRelationRepository.class), documents, mock(KnowledgeDifficultyScaleRepository.class),
                mock(KnowledgeDocRelationRepository.class), mock(KnowledgeChunkRepository.class), audit);
        assertThrows(ServiceException.class, () -> service.replaceMembers(actor(7L), 30L,
                List.of(new KnowledgeSpaceService.MemberRequest("USER", 8L, "INVALID"))));
        when(knowledge.findBySpace(new TenantId(7L), 30L)).thenReturn(List.of(new com.shiyu.ai.knowledge.domain.model.KnowledgeBO()));
        assertThrows(ServiceException.class, () -> service.delete(actor(7L), 30L));
    }

    @Test
    void createsOrRepairsDefaultSpaceAndInitializesTenantDefaults() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeAuditService audit = mock(KnowledgeAuditService.class);
        KnowledgeRepository knowledge = mock(KnowledgeRepository.class);
        KnowledgeRelationRepository relations = mock(KnowledgeRelationRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeDocRelationRepository docRelations = mock(KnowledgeDocRelationRepository.class);
        KnowledgeChunkRepository chunks = mock(KnowledgeChunkRepository.class);
        KnowledgeSpaceBO existing = new KnowledgeSpaceBO(); existing.setId(1L); existing.setCode("default"); existing.setTenantId(7L);
        when(repository.findSpaceByTenantAndCode(new TenantId(7L), "default")).thenReturn(existing);
        KnowledgeSpaceServiceImpl service = new KnowledgeSpaceServiceImpl(repository, knowledge, relations, documents,
                mock(KnowledgeDifficultyScaleRepository.class), docRelations, chunks, audit);
        assertEquals(1L, service.ensureDefaultSpace(actor(7L)).id());
        verify(repository).updateSpace(new TenantId(7L), existing);
        verify(knowledge).assignDefaultSpace(new TenantId(7L), 1L);
        verify(relations).assignDefaultSpace(new TenantId(7L), 1L);
        verify(documents).assignDefaultSpace(new TenantId(7L), 1L);
        verify(docRelations).assignDefaultSpace(new TenantId(7L), 1L);
        verify(chunks).assignDefaultSpace(new TenantId(7L), 1L);

        when(repository.findSpaceByTenantAndCode(new TenantId(8L), "default")).thenReturn(null);
        service.initializeTenantDefaults(new TenantId(8L));
        verify(repository).insertSpace(eq(new TenantId(8L)), any(KnowledgeSpaceBO.class));
        assertThrows(ServiceException.class, () -> service.initializeTenantDefaults((TenantId) null));
    }

    @Test
    void readsDifficultyScaleMembersAndAccessibleSpaces() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeDifficultyScaleRepository scales = mock(KnowledgeDifficultyScaleRepository.class);
        KnowledgeSpaceBO space = new KnowledgeSpaceBO(); space.setId(40L); space.setTenantId(7L); space.setCode("space"); space.setName("Space"); space.setAccessMode("TENANT"); space.setDifficultyScaleId(2L);
        when(repository.findSpaceByTenant(new TenantId(7L), 40L)).thenReturn(space);
        KnowledgeDifficultyScaleBO scale = new KnowledgeDifficultyScaleBO(); scale.setId(2L); scale.setCode("basic"); scale.setName("Basic"); scale.setLevelCount(1);
        KnowledgeDifficultyScaleLevelBO level = new KnowledgeDifficultyScaleLevelBO(); level.setLevel(1); level.setLabel("Easy");
        when(scales.findScale(new TenantId(7L), 2L)).thenReturn(scale);
        when(scales.findLevels(new TenantId(7L), 2L)).thenReturn(List.of(level));
        KnowledgeSpaceMemberBO member = new KnowledgeSpaceMemberBO(); member.setId(5L); member.setSpaceId(40L); member.setPrincipalType("USER"); member.setPrincipalId(8L); member.setSpaceRole("ADMIN");
        when(repository.findMembers(new TenantId(7L), 40L)).thenReturn(List.of(member));
        when(repository.hasMember(eq(new TenantId(7L)), eq(40L), eq("USER"), eq(8L), any())).thenReturn(true);
        when(repository.findActiveSpacesByTenant(new TenantId(7L))).thenReturn(List.of(space));
        KnowledgeSpaceServiceImpl service = new KnowledgeSpaceServiceImpl(repository, mock(KnowledgeRepository.class),
                mock(KnowledgeRelationRepository.class), mock(KnowledgeDocumentRepository.class), scales,
                mock(KnowledgeDocRelationRepository.class), mock(KnowledgeChunkRepository.class), mock(KnowledgeAuditService.class));
        assertEquals(1, service.difficultyScale(actor(7L), 40L).levels().size());
        assertEquals(1, service.members(actor(7L), 40L).size());
        assertEquals(1, service.accessibleSpaces(actor(7L)).size());
    }

    @Test
    void updatesAllMutableSpaceSettingsAndRejectsInvalidEnums() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeAuditService audit = mock(KnowledgeAuditService.class);
        KnowledgeSpaceBO space = new KnowledgeSpaceBO();
        space.setId(50L); space.setTenantId(7L); space.setCode("space"); space.setName("Old"); space.setAccessMode("PRIVATE");
        when(repository.findSpaceByTenant(new TenantId(7L), 50L)).thenReturn(space);
        when(repository.hasMember(eq(new TenantId(7L)), eq(50L), eq("USER"), eq(8L), any())).thenReturn(true);
        KnowledgeSpaceServiceImpl service = service(repository, audit);
        var request = new KnowledgeSpaceService.UpdateSpaceRequest(" New ", "desc", "domain", "TENANT", "REQUIRED", "REQUIRED",
                3L, "embed", "rerank", "paragraph", 1200, 50, 0);
        assertEquals("New", service.update(actor(7L), 50L, request).name());
        assertEquals("TENANT", space.getAccessMode());
        assertEquals("REQUIRED", space.getReviewMode());
        assertEquals(1200, space.getChunkSize());
        verify(repository).updateSpace(new TenantId(7L), space);
        verify(audit).record(any(), eq(50L), eq("SPACE"), eq(50L), eq("UPDATE"), eq(request));
        var invalid = new KnowledgeSpaceService.UpdateSpaceRequest(null, null, null, "INVALID", null, null,
                null, null, null, null, null, null, null);
        assertThrows(ServiceException.class, () -> service.update(actor(7L), 50L, invalid));
    }

    private KnowledgeSpaceServiceImpl service(KnowledgeEnterpriseRepository repository) {
        return service(repository, mock(KnowledgeAuditService.class));
    }

    private KnowledgeSpaceServiceImpl service(KnowledgeEnterpriseRepository repository, KnowledgeAuditService audit) {
        return new KnowledgeSpaceServiceImpl(
                repository,
                mock(KnowledgeRepository.class),
                mock(KnowledgeRelationRepository.class),
                mock(KnowledgeDocumentRepository.class),
                mock(KnowledgeDifficultyScaleRepository.class),
                mock(KnowledgeDocRelationRepository.class),
                mock(KnowledgeChunkRepository.class), audit);
    }

    private ActorContext actor(long tenantId) {
        return new ActorContext(new TenantId(tenantId), new UserId(8L), new RoleId(9L), false);
    }
}
