package com.shiyu.ai.knowledge.web;

import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class KnowledgeSpaceControllerCoverageTest {
    private final KnowledgeSpaceService service = mock(KnowledgeSpaceService.class);
    private final KnowledgeSpaceController controller = new KnowledgeSpaceController(service);

    @BeforeEach
    void actor() {
        UserContext context = new UserContext();
        context.setUserId(8L); context.setCurrentTenantId(7L); context.setHomeTenantId(7L);
        UserContextHolder.setContext(context);
    }

    @AfterEach
    void clear() { UserContextHolder.clearContext(); }

    @Test
    void mapsSpaceQueriesMutationsAndMembers() {
        KnowledgeSpaceService.CreateSpaceRequest create = new KnowledgeSpaceService.CreateSpaceRequest(
                "main", "Main", "general", null, "PRIVATE", "MANUAL", "NONE", null,
                "default", null, "fixed", 500, 50);
        KnowledgeSpaceService.UpdateSpaceRequest update = new KnowledgeSpaceService.UpdateSpaceRequest(
                "Renamed", null, "general", "PRIVATE", "MANUAL", "NONE", null,
                "default", null, "fixed", 500, 50, 1);
        KnowledgeSpaceService.MemberRequest member = new KnowledgeSpaceService.MemberRequest("USER", 8L, "EDITOR");
        when(service.page(any(), eq(1), eq(100), anyString(), anyString())).thenReturn(null);
        when(service.accessibleSpaces(any())).thenReturn(List.of());
        when(service.get(any(), eq(3L))).thenReturn(null);
        when(service.difficultyScale(any(), eq(3L))).thenReturn(null);
        when(service.create(any(), same(create))).thenReturn(null);
        when(service.update(any(), eq(3L), same(update))).thenReturn(null);
        when(service.members(any(), eq(3L))).thenReturn(List.of());
        when(service.ensureDefaultSpace(any())).thenReturn(null);
        assertTrue(controller.page(1, 200, "q", "general", "v1").isSuccess());
        assertTrue(controller.options().isSuccess());
        assertTrue(controller.get(3L, "1").isSuccess());
        assertTrue(controller.difficultyScale(3L, "1").isSuccess());
        assertTrue(controller.create(create, "1").isSuccess());
        assertTrue(controller.update(3L, update, "1").isSuccess());
        assertTrue(controller.members(3L, "1").isSuccess());
        assertTrue(controller.replaceMembers(3L, List.of(member), "1").isSuccess());
        assertTrue(controller.ensureDefault("1").isSuccess());
        controller.delete(3L, "1");
        verify(service).delete(any(), eq(3L));
    }

    @Test
    void rejectsUnsupportedSpaceApiVersion() {
        assertThrows(RuntimeException.class, () -> controller.page(1, 20, null, null, "2"));
        assertThrows(RuntimeException.class, () -> controller.create(
                new KnowledgeSpaceService.CreateSpaceRequest("main", "Main", null, null, null, null, null, null, null, null, null, 500, 0), "bad"));
    }
}
