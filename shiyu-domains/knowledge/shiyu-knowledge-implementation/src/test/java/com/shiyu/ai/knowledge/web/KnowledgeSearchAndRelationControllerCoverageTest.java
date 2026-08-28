package com.shiyu.ai.knowledge.web;

import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.knowledge.index.KnowledgeIndexService;
import com.shiyu.ai.knowledge.service.KnowledgeDocumentRelationService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class KnowledgeSearchAndRelationControllerCoverageTest {
    private final KnowledgeIndexService index = mock(KnowledgeIndexService.class);
    private final KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
    private final KnowledgeDocumentRelationService relations = mock(KnowledgeDocumentRelationService.class);

    @BeforeEach
    void actor() {
        UserContext context = new UserContext();
        context.setUserId(8L); context.setCurrentTenantId(7L); context.setHomeTenantId(7L);
        UserContextHolder.setContext(context);
    }

    @AfterEach
    void clear() { UserContextHolder.clearContext(); }

    @Test
    void mapsSearchDefaultsExplicitOptionsAndRebuild() {
        KnowledgeSearchController controller = new KnowledgeSearchController(index, spaces);
        when(index.hybridSearch(any(ActorContext.class), eq(3L), anyString(), anyString(), anyInt(), anyDouble(), anyBoolean())).thenReturn(List.of());
        when(index.rebuild(eq(new TenantId(7L)), eq(3L))).thenReturn(101L);
        KnowledgeSearchController.SearchRequest defaults = new KnowledgeSearchController.SearchRequest(3L, "query", null, null, null, null);
        KnowledgeSearchController.SearchRequest explicit = new KnowledgeSearchController.SearchRequest(3L, "query", "vector", 10, 0.5, true);
        assertTrue(controller.search(defaults, "1").isSuccess());
        assertTrue(controller.search(explicit, "v1").isSuccess());
        assertTrue(controller.rebuild(new KnowledgeSearchController.RebuildRequest(3L), "1").isSuccess());
        verify(spaces, times(2)).requireAccess(anyLong(), eq(KnowledgeSpaceService.SpaceRole.VIEWER), any());
        verify(spaces).requireAccess(eq(3L), eq(KnowledgeSpaceService.SpaceRole.ADMIN), any());
    }

    @Test
    void mapsDocumentRelationsAndRejectsVersion() {
        KnowledgeDocumentRelationController controller = new KnowledgeDocumentRelationController(relations);
        when(relations.listDocuments(any(), eq(5L))).thenReturn(List.of());
        when(relations.listPointIds(any(), eq(11L))).thenReturn(List.of(5L));
        when(relations.listDocumentRelations(any(), eq(11L))).thenReturn(List.of());
        KnowledgeDocumentRelationController.ReplaceRequest docs = new KnowledgeDocumentRelationController.ReplaceRequest(List.of(11L), "RELATED");
        KnowledgeDocumentRelationController.ReplacePointsRequest points = new KnowledgeDocumentRelationController.ReplacePointsRequest(List.of(5L), "RELATED");
        KnowledgeDocumentRelationController.ReplaceDocumentRelationsRequest all = new KnowledgeDocumentRelationController.ReplaceDocumentRelationsRequest(List.of(new KnowledgeDocumentRelationService.DocumentRelationRequest(12L, "RELATED")));
        assertTrue(controller.list(5L, "1").isSuccess());
        assertTrue(controller.replace(5L, docs, "1").isSuccess());
        assertEquals(List.of(5L), controller.listPoints(11L, "1").getData());
        assertTrue(controller.replacePoints(11L, points, "1").isSuccess());
        assertTrue(controller.listDocumentRelations(11L, "1").isSuccess());
        assertTrue(controller.replaceDocumentRelations(11L, all, "1").isSuccess());
        assertThrows(RuntimeException.class, () -> controller.list(5L, "2"));
    }
}
