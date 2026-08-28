package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.RoleId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDifficultyScaleBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceBO;
import com.shiyu.ai.knowledge.port.repository.KnowledgeChunkRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocRelationRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDifficultyScaleRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRelationRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.service.KnowledgeAuditService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KnowledgeSpaceServiceDeepCoverageTest {
    @Test
    void coversDefaultSpaceCreationAndExistingInitializationBranches() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeRepository knowledge = mock(KnowledgeRepository.class);
        KnowledgeRelationRepository relations = mock(KnowledgeRelationRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeDocRelationRepository docRelations = mock(KnowledgeDocRelationRepository.class);
        KnowledgeChunkRepository chunks = mock(KnowledgeChunkRepository.class);
        KnowledgeAuditService audit = mock(KnowledgeAuditService.class);
        ActorContext actor = actor(7L);
        KnowledgeSpaceServiceImpl service = new KnowledgeSpaceServiceImpl(repository, knowledge, relations,
                documents, mock(KnowledgeDifficultyScaleRepository.class), docRelations, chunks, audit);

        when(repository.findSpaceByTenantAndCode(new TenantId(7L), "default")).thenReturn(null);
        when(repository.insertSpace(eq(new TenantId(7L)), any(KnowledgeSpaceBO.class))).thenAnswer(invocation -> {
            KnowledgeSpaceBO value = invocation.getArgument(1);
            value.setId(11L);
            return value;
        });
        assertEquals(11L, service.ensureDefaultSpace(actor).id());
        verify(audit).record(eq(actor), eq(11L), eq("SPACE"), eq(11L), eq("CREATE_DEFAULT"), eq(null));

        KnowledgeSpaceBO existing = new KnowledgeSpaceBO();
        existing.setId(12L); existing.setCode("default"); existing.setTenantId(7L); existing.setDomainCode("GENERAL");
        when(repository.findSpaceByTenantAndCode(new TenantId(8L), "default")).thenReturn(existing);
        assertEquals(12L, service.ensureDefaultSpace(actor(8L)).id());
        verify(repository).findSpaceByTenantAndCode(new TenantId(8L), "default");
        when(repository.findSpaceByTenantAndCode(new TenantId(9L), "default")).thenReturn(existing);
        service.initializeTenantDefaults(new TenantId(9L));
        KnowledgeSpaceBO legacy = new KnowledgeSpaceBO();
        legacy.setId(13L); legacy.setTenantId(10L); legacy.setCode("default"); legacy.setDomainCode(" ");
        when(repository.findSpaceByTenantAndCode(new TenantId(10L), "default")).thenReturn(legacy);
        assertEquals("GENERAL", service.ensureDefaultSpace(actor(10L)).domainCode());
        verify(repository).updateSpace(new TenantId(10L), legacy);
    }

    @Test
    void coversSpaceValidationDeletionAndMemberBranches() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeRepository knowledge = mock(KnowledgeRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeAuditService audit = mock(KnowledgeAuditService.class);
        KnowledgeSpaceServiceImpl service = new KnowledgeSpaceServiceImpl(repository, knowledge,
                mock(KnowledgeRelationRepository.class), documents, mock(KnowledgeDifficultyScaleRepository.class),
                mock(KnowledgeDocRelationRepository.class), mock(KnowledgeChunkRepository.class), audit);
        ActorContext actor = actor(7L);
        KnowledgeSpaceBO space = new KnowledgeSpaceBO();
        space.setId(20L); space.setTenantId(7L); space.setCode("custom"); space.setAccessMode("PRIVATE");
        when(repository.findSpaceByTenant(new TenantId(7L), 20L)).thenReturn(space);
        when(repository.hasMember(eq(new TenantId(7L)), eq(20L), eq("USER"), eq(8L), any())).thenReturn(true);

        when(repository.findSpaceByTenantAndCode(new TenantId(7L), "same")).thenReturn(space);
        assertThrows(ServiceException.class, () -> service.create(actor,
                new KnowledgeSpaceService.CreateSpaceRequest("same", "Name", null, null, null, null,
                        null, null, null, null, null, null, null)));

        space.setCode("default");
        assertThrows(ServiceException.class, () -> service.delete(actor, 20L));
        space.setCode("custom");
        when(knowledge.findBySpace(new TenantId(7L), 20L)).thenReturn(List.of());
        when(documents.findBySpace(new TenantId(7L), 20L)).thenReturn(List.of());
        service.delete(actor, 20L);
        verify(repository).deleteSpace(new TenantId(7L), 20L);

        assertThrows(ServiceException.class, () -> service.replaceMembers(actor, 20L,
                List.of(new KnowledgeSpaceService.MemberRequest("GROUP", 1L, "ADMIN"))));
        assertThrows(ServiceException.class, () -> service.replaceMembers(actor, 20L,
                List.of(new KnowledgeSpaceService.MemberRequest("USER", 1L, "VIEWER"))));
        service.replaceMembers(actor, 20L,
                List.of(new KnowledgeSpaceService.MemberRequest("USER", 8L, "ADMIN")));
        verify(repository).replaceMembers(eq(new TenantId(7L)), eq(20L), any());
    }

    @Test
    void coversAccessModesDifficultyFailureAndPlatformAdmin() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeDifficultyScaleRepository scales = mock(KnowledgeDifficultyScaleRepository.class);
        KnowledgeSpaceBO space = new KnowledgeSpaceBO();
        space.setId(30L); space.setTenantId(7L); space.setCode("space"); space.setAccessMode("PRIVATE");
        space.setDifficultyScaleId(3L);
        when(repository.findSpaceByTenant(new TenantId(7L), 30L)).thenReturn(space);
        KnowledgeSpaceServiceImpl service = new KnowledgeSpaceServiceImpl(repository, mock(KnowledgeRepository.class),
                mock(KnowledgeRelationRepository.class), mock(KnowledgeDocumentRepository.class), scales,
                mock(KnowledgeDocRelationRepository.class), mock(KnowledgeChunkRepository.class), mock(KnowledgeAuditService.class));

        assertThrows(ServiceException.class, () -> service.requireAccess(30L, KnowledgeSpaceService.SpaceRole.VIEWER, null));
        assertThrows(ServiceException.class, () -> service.requireAccess(30L, KnowledgeSpaceService.SpaceRole.ADMIN, actor(7L)));
        assertEquals(30L, service.get(new ActorContext(new TenantId(7L), new UserId(8L), new RoleId(9L), true), 30L).id());
        when(repository.hasMember(eq(new TenantId(7L)), eq(30L), eq("USER"), eq(8L), any())).thenReturn(true);
        assertThrows(ServiceException.class, () -> service.difficultyScale(actor(7L), 30L));
        KnowledgeDifficultyScaleBO scale = new KnowledgeDifficultyScaleBO(); scale.setId(3L); scale.setCode("basic"); scale.setName("Basic");
        when(scales.findScale(new TenantId(7L), 3L)).thenReturn(scale);
        when(scales.findLevels(new TenantId(7L), 3L)).thenReturn(List.of());
        assertEquals(3L, service.difficultyScale(new ActorContext(new TenantId(7L), new UserId(8L), null, true), 30L).id());
    }

    @Test
    void coversCreateUpdateTenantAccessAndAccessibleFiltering() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeRepository knowledge = mock(KnowledgeRepository.class);
        KnowledgeSpaceBO space = new KnowledgeSpaceBO();
        space.setId(40L); space.setTenantId(7L); space.setCode("custom"); space.setAccessMode("TENANT");
        when(repository.findSpaceByTenantAndCode(new TenantId(7L), "custom-space")).thenReturn(null);
        when(repository.insertSpace(eq(new TenantId(7L)), any(KnowledgeSpaceBO.class))).thenAnswer(invocation -> {
            KnowledgeSpaceBO value = invocation.getArgument(1); value.setId(40L); return value;
        });
        KnowledgeSpaceServiceImpl service = new KnowledgeSpaceServiceImpl(repository, knowledge,
                mock(KnowledgeRelationRepository.class), mock(KnowledgeDocumentRepository.class),
                mock(KnowledgeDifficultyScaleRepository.class), mock(KnowledgeDocRelationRepository.class),
                mock(KnowledgeChunkRepository.class), mock(KnowledgeAuditService.class));
        ActorContext actor = actor(7L);
        var request = new KnowledgeSpaceService.CreateSpaceRequest(" Custom Space ", "  Name  ", "sales",
                "desc", "tenant", "required", "required", null, null, null, "paragraph", null, null);
        assertEquals("custom-space", service.create(actor, request).code());

        when(repository.findSpaceByTenant(new TenantId(7L), 40L)).thenReturn(space);
        when(repository.hasMember(eq(new TenantId(7L)), eq(40L), eq("USER"), eq(8L), any())).thenReturn(true);
        var update = new KnowledgeSpaceService.UpdateSpaceRequest(" New ", "description", "ops", "PRIVATE",
                "DIRECT", "OPTIONAL", 2L, "embed", "rerank", "heading", 900, 120, 1);
        assertEquals("New", service.update(actor, 40L, update).name());
        assertEquals(40L, service.get(actor, 40L).id());
        when(repository.findSpaceByTenant(new TenantId(7L), 404L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> service.get(actor, 404L));

        when(repository.findActiveSpacesByTenant(new TenantId(7L))).thenReturn(List.of(space));
        assertEquals(1, service.accessibleSpaces(actor).size());
        assertThrows(ServiceException.class, () -> service.initializeTenantDefaults((TenantId) null));
    }

    @Test
    void coversPrivateMembershipFallbacksDefaultsAndInvalidSpaceInputs() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeRepository knowledge = mock(KnowledgeRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeSpaceBO privateSpace = new KnowledgeSpaceBO();
        privateSpace.setId(50L); privateSpace.setTenantId(7L); privateSpace.setCode("private");
        privateSpace.setAccessMode("PRIVATE"); privateSpace.setStatus(1); privateSpace.setDelFlag(0);
        KnowledgeSpaceBO deniedSpace = new KnowledgeSpaceBO();
        deniedSpace.setId(51L); deniedSpace.setTenantId(7L); deniedSpace.setCode("denied");
        deniedSpace.setAccessMode("PRIVATE"); deniedSpace.setStatus(1); deniedSpace.setDelFlag(0);
        ActorContext actor = actor(7L);
        KnowledgeSpaceServiceImpl service = new KnowledgeSpaceServiceImpl(repository, knowledge,
                mock(KnowledgeRelationRepository.class), documents, mock(KnowledgeDifficultyScaleRepository.class),
                mock(KnowledgeDocRelationRepository.class), mock(KnowledgeChunkRepository.class),
                mock(KnowledgeAuditService.class));

        when(repository.findSpaceByTenant(new TenantId(7L), 50L)).thenReturn(privateSpace);
        doReturn(false).when(repository).hasMember(eq(new TenantId(7L)), eq(50L), eq("USER"),
                eq(8L), any());
        doReturn(true).when(repository).hasMember(eq(new TenantId(7L)), eq(50L), eq("ROLE"),
                isNull(), any());
        assertDoesNotThrow(() -> service.requireAccess(50L, KnowledgeSpaceService.SpaceRole.ADMIN,
                new ActorContext(new TenantId(7L), new UserId(8L), null, false)));

        doReturn(true).when(repository).hasMember(eq(new TenantId(7L)), eq(50L), eq("USER"),
                eq(8L), any());
        assertEquals(50L, service.get(actor, 50L).id());
        when(repository.findActiveSpacesByTenant(new TenantId(7L))).thenReturn(List.of(privateSpace, deniedSpace));
        when(repository.findSpaceByTenant(new TenantId(7L), 51L)).thenReturn(deniedSpace);
        doReturn(false).when(repository).hasMember(eq(new TenantId(7L)), eq(51L), eq("USER"),
                eq(8L), any());
        doReturn(false).when(repository).hasMember(eq(new TenantId(7L)), eq(51L), eq("ROLE"),
                eq(9L), any());
        assertEquals(1, service.accessibleSpaces(actor).size());
        when(repository.pageSpacesByTenant(new TenantId(7L), 1, 10, null, null))
                .thenReturn(new PageData<>(List.of(privateSpace, deniedSpace), 2));
        assertEquals(1, service.page(actor, 1, 10, null).getItems().size());

        assertThrows(ServiceException.class, () -> service.create(actor,
                new KnowledgeSpaceService.CreateSpaceRequest("bad", "Name", "bad domain", null,
                        null, null, null, null, null, null, null, null, null)));
        assertThrows(ServiceException.class, () -> service.create(actor,
                new KnowledgeSpaceService.CreateSpaceRequest("bad", "Name", null, null,
                        "INVALID", null, null, null, null, null, null, null, null)));

        when(repository.findSpaceByTenantAndCode(new TenantId(7L), "created")).thenReturn(null);
        when(repository.insertSpace(eq(new TenantId(7L)), any(KnowledgeSpaceBO.class))).thenAnswer(invocation -> {
            KnowledgeSpaceBO value = invocation.getArgument(1); value.setId(52L); return value;
        });
        assertEquals("created", service.create(actor,
                new KnowledgeSpaceService.CreateSpaceRequest("created", "Name", null, null,
                        null, null, null, null, null, null, null, null, null)).code());

        when(repository.findSpaceByTenantAndCode(new TenantId(7L), "tuned")).thenReturn(null);
        when(repository.insertSpace(eq(new TenantId(7L)), any(KnowledgeSpaceBO.class))).thenAnswer(invocation -> {
            KnowledgeSpaceBO value = invocation.getArgument(1); value.setId(53L); return value;
        });
        assertEquals(53L, service.create(actor,
                new KnowledgeSpaceService.CreateSpaceRequest("tuned", "Tuned", "ops", "desc",
                        "TENANT", "DIRECT", "REQUIRED", 9L, "embed", "rerank", "paragraph", 640, 32)).id());

        // Cover the null request collection and the document-only deletion guard.
        assertThrows(ServiceException.class, () -> service.replaceMembers(actor, 50L, null));
        when(knowledge.findBySpace(new TenantId(7L), 50L)).thenReturn(List.of());
        when(documents.findBySpace(new TenantId(7L), 50L)).thenReturn(List.of(new KnowledgeDocumentBO()));
        assertThrows(ServiceException.class, () -> service.delete(actor, 50L));

        doReturn(true).when(repository).hasMember(eq(new TenantId(7L)), eq(50L), eq("USER"),
                eq(8L), any());
        assertEquals("private", service.update(actor, 50L,
                new KnowledgeSpaceService.UpdateSpaceRequest(null, null, null, null, null, null,
                        null, null, null, null, null, null, null)).code());
        assertEquals("private", service.update(actor, 50L,
                new KnowledgeSpaceService.UpdateSpaceRequest(" ", null, null, null, null, null,
                        null, null, null, null, null, null, null)).code());
        when(knowledge.findBySpace(new TenantId(7L), 50L)).thenReturn(List.of(new KnowledgeBO()));
        assertThrows(ServiceException.class, () -> service.delete(actor, 50L));
    }

    @Test
    void coversNormalizationAndMissingSpaceHelperBranches() throws Exception {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeSpaceServiceImpl service = new KnowledgeSpaceServiceImpl(repository,
                mock(KnowledgeRepository.class), mock(KnowledgeRelationRepository.class),
                mock(KnowledgeDocumentRepository.class), mock(KnowledgeDifficultyScaleRepository.class),
                mock(KnowledgeDocRelationRepository.class), mock(KnowledgeChunkRepository.class),
                mock(KnowledgeAuditService.class));
        Method normalizeEnum = KnowledgeSpaceServiceImpl.class.getDeclaredMethod(
                "normalizeEnum", String.class, String.class, Set.class, String.class);
        normalizeEnum.setAccessible(true);
        assertThrows(Exception.class, () -> normalizeEnum.invoke(service, null, null, Set.of(), "mode"));
        Method normalizeDomain = KnowledgeSpaceServiceImpl.class.getDeclaredMethod(
                "normalizeDomainCode", String.class, String.class);
        normalizeDomain.setAccessible(true);
        assertEquals(null, normalizeDomain.invoke(service, null, null));
        Method defaultText = KnowledgeSpaceServiceImpl.class.getDeclaredMethod(
                "defaultText", String.class, String.class);
        defaultText.setAccessible(true);
        assertEquals("fallback", defaultText.invoke(service, " ", "fallback"));
        assertEquals("value", defaultText.invoke(service, "value", "fallback"));
        Method requireSpace = KnowledgeSpaceServiceImpl.class.getDeclaredMethod(
                "requireSpace", ActorContext.class, Long.class);
        requireSpace.setAccessible(true);
        assertThrows(Exception.class, () -> requireSpace.invoke(service, actor(7L), 999L));
    }

    private ActorContext actor(long tenantId) {
        return new ActorContext(new TenantId(tenantId), new UserId(8L), new RoleId(9L), false);
    }
}
