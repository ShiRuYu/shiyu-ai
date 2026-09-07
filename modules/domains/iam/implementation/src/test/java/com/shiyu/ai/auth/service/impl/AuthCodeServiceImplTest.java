package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.api.request.AuthCodeRequest;
import com.shiyu.ai.auth.domain.model.AuthCodeBO;
import com.shiyu.ai.auth.domain.model.TenantAuthCodeBO;
import com.shiyu.ai.auth.domain.model.TenantBO;
import com.shiyu.ai.auth.port.repository.AuthCodeRepository;
import com.shiyu.ai.auth.port.repository.RoleRepository;
import com.shiyu.ai.auth.port.repository.TenantRepository;
import com.shiyu.ai.auth.request.AuthCodePageRequest;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthCodeServiceImplTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(5), new UserId(9), false);
    private final AuthCodeRepository repository = mock(AuthCodeRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final TenantRepository tenantRepository = mock(TenantRepository.class);
    private final AuthCodeServiceImpl service = new AuthCodeServiceImpl(repository, roleRepository, tenantRepository);

    @Test
    void listsPagesCreatesUpdatesAndDeletesTenantCodes() {
        AuthCodeBO code = code(1L, "agent:read");
        when(repository.selectByTenantId(ACTOR.tenantId())).thenReturn(List.of(code));
        AuthCodePageRequest page = new AuthCodePageRequest(); page.setPageNum(1); page.setPageSize(10); page.setCode("agent");
        assertEquals(1, service.list(ACTOR).size()); assertEquals(1, service.page(ACTOR, page).getItems().size());
        AuthCodeRequest request = request("agent:write"); AuthCodeBO saved = code(2L, "agent:write");
        when(repository.existsByCode("agent:write", null)).thenReturn(false); when(repository.insert(any())).thenReturn(saved);
        assertEquals(2L, service.create(ACTOR, request).getId()); verify(repository).insertTenantCode(any(TenantAuthCodeBO.class));
        when(repository.selectById(1L)).thenReturn(code); when(repository.isAvailable(1L, ACTOR.tenantId())).thenReturn(true); when(repository.existsByCode("agent:update", 1L)).thenReturn(false);
        assertTrue(service.update(ACTOR, 1L, request("agent:update")));
        when(repository.hasRoleAssignments(1L)).thenReturn(false); when(repository.countActiveTenantLinks(1L)).thenReturn(0L);
        assertTrue(service.delete(ACTOR, 1L)); verify(repository).deleteTenantCode(ACTOR.tenantId(), 1L);
    }

    @Test
    void grantsReplacesRevokesOnlyWithinTenantRoleScope() {
        TenantBO tenant = new TenantBO(); tenant.setStatus(1); tenant.setDelFlag(0);
        when(roleRepository.isRoleOwnedByTenant(7L, new TenantId(5L))).thenReturn(true); when(tenantRepository.selectById(5L)).thenReturn(tenant); when(tenantRepository.selectDescendantIds(new TenantId(5L))).thenReturn(List.of(5L));
        AuthCodeBO code = code(3L, "agent:read"); when(repository.selectAvailableByIds(List.of(3L), ACTOR.tenantId())).thenReturn(List.of(code)); when(repository.selectByRoleIdAndTenantId(7L, ACTOR.tenantId())).thenReturn(List.of());
        assertTrue(service.grant(ACTOR, 7L, ACTOR.tenantId(), List.of(3L, 3L))); verify(repository).insertRoleAssignments(any());
        when(repository.selectByTenantId(ACTOR.tenantId())).thenReturn(List.of(code)); assertTrue(service.replace(ACTOR, 7L, ACTOR.tenantId(), List.of("agent:read", "agent:read")));
        assertTrue(service.revoke(ACTOR, 7L, ACTOR.tenantId(), 3L)); verify(repository).deleteRoleAssignments(7L, ACTOR.tenantId(), 3L);
        when(tenantRepository.selectDescendantIds(new TenantId(5L))).thenReturn(List.of()); assertFalse(service.revoke(ACTOR, 7L, ACTOR.tenantId(), 3L));
    }

    @Test
    void rejectsInvalidScopeAndDuplicateCodes() {
        assertThrows(NullPointerException.class, () -> service.list(null));
        when(repository.existsByCode("agent:read", null)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.create(ACTOR, request("agent:read")));
        assertFalse(service.grant(ACTOR, 7L, ACTOR.tenantId(), List.of()));
    }

    @Test
    void coversAuthCodeValidationPaginationAndIdempotentGrantBranches() {
        AuthCodeBO moduleOnly = code(null, "agent");
        AuthCodeBO resourceCode = code(4L, "agent:document:read");
        AuthCodeBO nullCode = code(2L, null);
        nullCode.setName(null);
        when(repository.selectByTenantId(ACTOR.tenantId())).thenReturn(List.of(resourceCode, moduleOnly, nullCode));
        AuthCodePageRequest page = new AuthCodePageRequest();
        page.setPageNum(null); page.setPageSize(null); page.setCode(""); page.setName("");
        assertEquals(3, service.options(ACTOR).size());
        assertEquals(3, service.page(ACTOR, page).getTotal());
        page.setPageNum(99); page.setPageSize(2); page.setCode("missing");
        assertTrue(service.page(ACTOR, page).getItems().isEmpty());

        assertThrows(IllegalArgumentException.class, () -> service.create(ACTOR, request(null)));
        AuthCodeRequest tooLong = request("x".repeat(65));
        assertThrows(IllegalArgumentException.class, () -> service.create(ACTOR, tooLong));
        assertFalse(service.update(ACTOR, 99L, request("x")));
        when(repository.selectById(1L)).thenReturn(code(1L, "old"));
        when(repository.isAvailable(1L, ACTOR.tenantId())).thenReturn(false);
        assertFalse(service.update(ACTOR, 1L, request("x")));
        when(repository.isAvailable(1L, ACTOR.tenantId())).thenReturn(true);
        when(repository.existsByCode("x", 1L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.update(ACTOR, 1L, request("x")));

        when(repository.selectById(9L)).thenReturn(null);
        assertFalse(service.delete(ACTOR, 9L));
        when(repository.selectById(10L)).thenReturn(code(10L, "delete"));
        when(repository.isAvailable(10L, ACTOR.tenantId())).thenReturn(true);
        when(repository.hasRoleAssignments(10L)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> service.delete(ACTOR, 10L));

        when(roleRepository.isRoleOwnedByTenant(7L, new TenantId(5L))).thenReturn(true);
        TenantBO tenant = new TenantBO(); tenant.setStatus(1); tenant.setDelFlag(0);
        when(tenantRepository.selectById(5L)).thenReturn(tenant);
        when(tenantRepository.selectDescendantIds(new TenantId(5L))).thenReturn(List.of(5L));
        when(repository.selectByRoleIdAndTenantId(7L, ACTOR.tenantId())).thenReturn(List.of(code(3L, "existing")));
        when(repository.selectAvailableByIds(List.of(3L, 4L), ACTOR.tenantId())).thenReturn(List.of(code(3L, "existing")));
        assertFalse(service.grant(ACTOR, 7L, ACTOR.tenantId(), Arrays.asList(3L, 4L, null)));
        assertFalse(service.grant(ACTOR, 7L, ACTOR.tenantId(), null));
        assertFalse(service.replace(ACTOR, 7L, ACTOR.tenantId(), List.of("missing")));
        assertTrue(service.replace(ACTOR, 7L, ACTOR.tenantId(), null));
        assertTrue(service.revoke(ACTOR, 7L, ACTOR.tenantId(), null));

        when(roleRepository.isRoleOwnedByTenant(7L, new TenantId(5L))).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.listRoleAuthCodes(ACTOR, 7L, ACTOR.tenantId()));
        TenantBO disabled = new TenantBO(); disabled.setStatus(0); disabled.setDelFlag(0);
        when(roleRepository.isRoleOwnedByTenant(7L, new TenantId(5L))).thenReturn(true);
        when(tenantRepository.selectById(5L)).thenReturn(disabled);
        assertFalse(service.revoke(ACTOR, 7L, ACTOR.tenantId(), 3L));
    }

    @Test
    void coversEveryAuthorizationCodeTenantScopeGuard() {
        // Role ownership and requested tenant are checked before touching the
        // tenant repository.
        assertFalse(service.grant(ACTOR, 7L, null, List.of(1L)));
        when(roleRepository.isRoleOwnedByTenant(7L, new TenantId(5L))).thenReturn(true);
        when(tenantRepository.selectById(5L)).thenReturn(null);
        assertFalse(service.revoke(ACTOR, 7L, ACTOR.tenantId(), 1L));

        TenantBO missingStatus = new TenantBO();
        missingStatus.setDelFlag(0);
        when(tenantRepository.selectById(5L)).thenReturn(missingStatus);
        assertFalse(service.revoke(ACTOR, 7L, ACTOR.tenantId(), 1L));

        TenantBO deleted = new TenantBO();
        deleted.setStatus(1);
        deleted.setDelFlag(2);
        when(tenantRepository.selectById(5L)).thenReturn(deleted);
        assertFalse(service.revoke(ACTOR, 7L, ACTOR.tenantId(), 1L));

        TenantBO active = new TenantBO();
        active.setStatus(1);
        active.setDelFlag(null);
        when(tenantRepository.selectById(5L)).thenReturn(active);
        when(tenantRepository.selectDescendantIds(new TenantId(5L))).thenReturn(List.of(8L));
        assertFalse(service.revoke(ACTOR, 7L, ACTOR.tenantId(), 1L));

        when(tenantRepository.selectDescendantIds(new TenantId(5L))).thenReturn(List.of(5L));
        assertTrue(service.revoke(ACTOR, 7L, ACTOR.tenantId(), 1L));
    }

    private static AuthCodeBO code(Long id, String value) { AuthCodeBO code = new AuthCodeBO(); code.setId(id); code.setCode(value); code.setName(value); code.setStatus(1); return code; }
    private static AuthCodeRequest request(String value) { AuthCodeRequest request = new AuthCodeRequest(); request.setCode(value); request.setName(value); return request; }
}
