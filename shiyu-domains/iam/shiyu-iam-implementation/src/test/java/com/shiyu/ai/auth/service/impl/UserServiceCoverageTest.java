package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.model.*;
import com.shiyu.ai.auth.port.repository.*;
import com.shiyu.ai.auth.request.UserRequest;
import com.shiyu.ai.auth.request.UserTenantRoleRequest;
import com.shiyu.ai.auth.service.MenuService;
import com.shiyu.ai.auth.vo.UserVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.common.core.utils.PasswordUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class UserServiceCoverageTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(7), new UserId(11), false);

    @Test
    void coversTenantScopedDetailsListsCreateUpdateAndDeletion() {
        UserRepository users = mock(UserRepository.class); RoleRepository roles = mock(RoleRepository.class); UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRepository tenants = mock(TenantRepository.class); TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class); MenuService menu = mock(MenuService.class);
        UserServiceImpl service = new UserServiceImpl(users, roles, assignments, tenants, tenantRoles, menu);
        UserBO user = user(20L, "alice"); RoleBO role = role(30L, 7L, "teacher"); UserScopeRoleBO assignment = assignment(20L, 30L, 7L, 1, 0);
        user.setExtInfo("{\"currentRole\":{\"roleId\":30}}");
	        when(users.isUserInScope(20L, new TenantId(7L))).thenReturn(true, true, true, false); when(users.selectById(20L)).thenReturn(user); when(users.selectRolesByUserId(20L)).thenReturn(List.of(role)); when(assignments.selectByUserId(20L)).thenReturn(List.of(assignment));
        UserVO vo = new UserVO(); vo.setId(20L);
        when(users.selectPage(eq(ACTOR.tenantId()), any(), any(), eq("ali"))).thenReturn(Pair.of(1L, List.of(user))); when(assignments.selectByUserIds(List.of(20L))).thenReturn(List.of(assignment));
        UserBO inserted = user(21L, "new"); when(users.insert(any(UserBO.class))).thenReturn(inserted); when(users.selectById(21L)).thenReturn(inserted); when(users.update(any(UserBO.class))).thenReturn(true); when(users.deleteById(20L)).thenReturn(true); when(roles.isRoleOwnedByTenant(30L, new TenantId(7L))).thenReturn(true);
        UserRequest request = new UserRequest(); request.setUsername("new"); request.setPassword("secret");
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(UserBO.class), eq(UserVO.class))).thenReturn(vo);
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(UserVO.class))).thenReturn(List.of(vo));
            mapper.when(() -> MapstructUtils.convert(any(UserRequest.class), eq(UserBO.class))).thenReturn(user(21L, "new"));
            assertNotNull(service.detailView(ACTOR, 20L)); assertEquals(1, service.getUserList(ACTOR, "ali", 1, 10).getTotal());
            assertNotNull(service.createUser(ACTOR, request, new Long[]{30L, 30L}, 7L)); assertTrue(service.updateUser(ACTOR, 20L, request, null, 7L));
        }
        assertTrue(service.deleteUser(ACTOR, 20L)); assertFalse(service.deleteUser(ACTOR, 20L)); verify(users).deleteById(20L);
    }

    @Test
    void coversPasswordAndTenantAssignmentGovernance() {
        UserRepository users = mock(UserRepository.class); RoleRepository roles = mock(RoleRepository.class); UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class); TenantRepository tenants = mock(TenantRepository.class); TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class); MenuService menu = mock(MenuService.class);
        UserServiceImpl service = new UserServiceImpl(users, roles, assignments, tenants, tenantRoles, menu); UserBO user = user(11L, "self"); user.setPassword(PasswordUtils.encode("old"));
	        when(users.isUserInScope(11L, new TenantId(7L))).thenReturn(true); when(users.selectById(11L)).thenReturn(user); when(users.update(any(UserBO.class))).thenReturn(true);
        assertFalse(service.changePassword(ACTOR, 12L, "old", "new")); assertFalse(service.changePassword(ACTOR, 11L, "bad", "new")); assertTrue(service.changePassword(ACTOR, 11L, "old", "new"));
        assertNotNull(service.resetUserPassword(ACTOR, 11L, "next"));
        TenantBO tenant = new TenantBO(); tenant.setId(7L); tenant.setName("Tenant"); tenant.setStatus(1); tenant.setDelFlag(0); RoleBO role = role(30L, 7L, "teacher");
        when(tenants.selectDescendantIds(new TenantId(7L))).thenReturn(List.of(7L)); when(assignments.selectByUserId(11L)).thenReturn(List.of(assignment(11L, 30L, 7L, 1, 0))); when(tenantRoles.selectTenantById(new TenantId(7L))).thenReturn(tenant); when(tenantRoles.selectRoleById(30L)).thenReturn(role); when(roles.isRoleOwnedByTenant(30L, new TenantId(7L))).thenReturn(true); assertEquals(1, service.getTenantAssignments(ACTOR, 11L).size());
	        UserTenantRoleRequest item = new UserTenantRoleRequest(); item.setTenantId(7L); item.setRoleId(30L); assertTrue(service.replaceTenantAssignments(ACTOR, 11L, List.of(item, item))); verify(assignments).deleteByUserIdAndTenantId(11L, new TenantId(7L)); verify(assignments).insert(any());
    }

    @Test
    void parentAdminAndInvalidScopesAreRejectedOrDelegatedSafely() {
        UserRepository users = mock(UserRepository.class); RoleRepository roles = mock(RoleRepository.class); UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class); TenantRepository tenants = mock(TenantRepository.class); TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class); MenuService menu = mock(MenuService.class);
        UserServiceImpl service = new UserServiceImpl(users, roles, assignments, tenants, tenantRoles, menu); ActorContext parent = new ActorContext(new TenantId(7), new UserId(11), null, "PARENT_SUPER_ADMIN", new TenantId(7), "PARENT_SUPER_ADMIN", true);
	        UserBO user = user(20L, "child"); user.setExtInfo("{}"); RoleBO delegated = role(99L, 7L, "tenant_super"); when(users.isUserInScope(20L, new TenantId(7L))).thenReturn(true); when(users.selectById(20L)).thenReturn(user); when(assignments.selectByUserId(20L)).thenReturn(List.of()); when(tenantRoles.selectTenantSuperRole(new TenantId(7L))).thenReturn(delegated);
        UserVO vo = new UserVO(); vo.setId(20L);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) { mapper.when(() -> MapstructUtils.convert(any(RoleBO.class), eq(RoleBO.class))).thenReturn(delegated); mapper.when(() -> MapstructUtils.convert(any(UserBO.class), eq(UserVO.class))).thenReturn(vo); assertNotNull(service.detailView(parent, 20L)); }
        assertFalse(service.replaceTenantAssignments(ACTOR, 99L, List.of()));
    }

    @Test
    void preservesExistingFieldsOnPartialUpdateAndRejectsInvalidRoleScopes() {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRepository tenants = mock(TenantRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        MenuService menu = mock(MenuService.class);
        UserServiceImpl service = new UserServiceImpl(users, roles, assignments, tenants, tenantRoles, menu);
        UserBO existing = user(20L, "existing");
        existing.setPassword(PasswordUtils.encode("old"));
        existing.setNickName("Existing");
        existing.setEmail("old@example.test");
	        when(users.isUserInScope(20L, new TenantId(7L))).thenReturn(true);
        when(users.selectById(20L)).thenReturn(existing);
        when(users.update(any(UserBO.class))).thenReturn(true);
        UserRequest patch = new UserRequest();
        UserBO mapped = user(null, null);
        mapped.setPassword(null);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(patch, UserBO.class)).thenReturn(mapped);
            assertTrue(service.updateUser(ACTOR, 20L, patch, null, 7L));
        }
        assertEquals("existing", mapped.getUsername());
        assertEquals("Existing", mapped.getNickName());
        assertEquals("old@example.test", mapped.getEmail());
        assertEquals(existing.getPassword(), mapped.getPassword());

	        when(users.isUserInScope(20L, new TenantId(7L))).thenReturn(false);
        assertNull(service.resetUserPassword(ACTOR, 20L, "new"));
        assertFalse(service.replaceTenantAssignments(ACTOR, 20L,
                List.of(new UserTenantRoleRequest())));
        verify(assignments, never()).insert(any());
    }

    @Test
    void rejectsMissingContextsTenantsAndRoleOwnershipBeforeMutation() {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRepository tenants = mock(TenantRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        UserServiceImpl service = new UserServiceImpl(users, roles, assignments, tenants, tenantRoles,
                mock(MenuService.class));
        UserRequest request = new UserRequest();
        assertThrows(NullPointerException.class, () -> service.detailView(null, 1L));
        assertThrows(IllegalArgumentException.class, () -> service.createUser(ACTOR, request, null, null));
        assertThrows(IllegalArgumentException.class, () -> service.updateUser(ACTOR, 1L, request, null, 0L));
        assertThrows(NullPointerException.class, () -> service.getUserList(null, null, 1, 10));

        UserBO created = user(30L, "created");
        when(users.insert(any(UserBO.class))).thenReturn(created);
        when(users.selectById(30L)).thenReturn(created);
        when(roles.isRoleOwnedByTenant(99L, new TenantId(7L))).thenReturn(false);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(request, UserBO.class)).thenReturn(user(null, "created"));
            assertThrows(IllegalArgumentException.class,
                    () -> service.createUser(ACTOR, request, new Long[]{99L}, 7L));
        }
	        verify(assignments, never()).deleteByUserIdAndTenantId(anyLong(), any(TenantId.class));

        UserScopeRoleBO inactive = assignment(30L, 99L, 7L, 0, 0);
        UserScopeRoleBO wrongTenant = assignment(30L, 98L, 8L, 1, 0);
	        when(users.isUserInScope(30L, new TenantId(7L))).thenReturn(true);
        when(tenants.selectDescendantIds(new TenantId(7L))).thenReturn(List.of(7L));
        when(assignments.selectByUserId(30L)).thenReturn(List.of(inactive, wrongTenant));
        assertTrue(service.getTenantAssignments(ACTOR, 30L).isEmpty());
    }

    @Test
    void coversUserDetailRoleFallbackAndManagementBoundaryBranches() {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRepository tenants = mock(TenantRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        MenuService menu = mock(MenuService.class);
        UserServiceImpl service = new UserServiceImpl(users, roles, assignments, tenants, tenantRoles, menu);

        assertNull(service.detailView(ACTOR, null));
        UserBO detailed = user(25L, "detailed");
        detailed.setExtInfo("{\"currentRole\":{\"roleId\":30,\"roleKey\":\"teacher\"}}");
        RoleBO selected = role(30L, 7L, "teacher");
	        when(users.isUserInScope(25L, new TenantId(7L))).thenReturn(true);
        when(users.selectById(25L)).thenReturn(detailed);
        when(assignments.selectByUserId(25L)).thenReturn(List.of(assignment(25L, 30L, 7L, 1, 0)));
        when(users.selectRolesByUserId(25L)).thenReturn(List.of(selected, selected));
        UserVO detailVo = new UserVO(); detailVo.setId(25L);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(UserBO.class), eq(UserVO.class))).thenReturn(detailVo);
            assertNotNull(service.detailView(ACTOR, 25L));

            UserRequest createRequest = new UserRequest();
            createRequest.setUsername("generated");
            createRequest.setPassword(" ");
            UserBO created = user(26L, "generated");
            when(users.insert(any(UserBO.class))).thenReturn(created);
            mapper.when(() -> MapstructUtils.convert(createRequest, UserBO.class)).thenReturn(created);
            assertNotNull(service.createUser(ACTOR, createRequest, null, 7L));
        }

        UserRequest patch = new UserRequest();
	        when(users.isUserInScope(25L, new TenantId(7L))).thenReturn(false, true);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(patch, UserBO.class)).thenReturn(new UserBO());
            assertFalse(service.updateUser(ACTOR, 25L, patch, null, 7L));
        }
        when(users.selectById(25L)).thenReturn(null);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(patch, UserBO.class)).thenReturn(new UserBO());
            assertFalse(service.updateUser(ACTOR, 25L, patch, null, 7L));
        }
	        when(users.isUserInScope(25L, new TenantId(7L))).thenReturn(true);
        when(users.selectById(25L)).thenReturn(null);
        assertNull(service.resetUserPassword(ACTOR, 25L, "new"));
        assertFalse(service.changePassword(ACTOR, 25L, "old", "new"));

	        when(users.isUserInScope(25L, new TenantId(7L))).thenReturn(true);
        when(tenants.selectDescendantIds(new TenantId(7L))).thenReturn(List.of(7L));
        UserScopeRoleBO active = assignment(25L, 30L, 7L, 1, 0);
        when(assignments.selectByUserId(25L)).thenReturn(List.of(active));
	        when(tenantRoles.selectTenantById(new TenantId(7L))).thenReturn(null);
        assertTrue(service.getTenantAssignments(ACTOR, 25L).isEmpty());

        UserTenantRoleRequest childAssignment = new UserTenantRoleRequest();
        childAssignment.setTenantId(8L); childAssignment.setRoleId(31L);
        assertFalse(service.replaceTenantAssignments(ACTOR, 25L, List.of(childAssignment)));
        assertFalse(service.replaceTenantAssignments(ACTOR, null, List.of()));
    }

    @Test
    void coversDelegatedDetailFallbackAndSelectedRoleTrustBoundaries() {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRepository tenants = mock(TenantRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        UserServiceImpl service = new UserServiceImpl(users, roles, assignments, tenants, tenantRoles,
                mock(MenuService.class));
        ActorContext delegatedActor = new ActorContext(new TenantId(7), new UserId(11), null,
                "tenant_super", new TenantId(7), "PARENT_SUPER_ADMIN", true);
        UserVO vo = new UserVO();

        UserBO fallback = user(31L, "fallback");
        fallback.setExtInfo("{\"switchMode\":\"PARENT_SUPER_ADMIN\",\"currentRole\":{\"roleId\":99,\"roleKey\":123}}");
        RoleBO ordinary = role(30L, 7L, "teacher");
	        when(users.isUserInScope(31L, new TenantId(7L))).thenReturn(true);
        when(users.selectById(31L)).thenReturn(fallback);
        when(assignments.selectByUserId(31L)).thenReturn(List.of(assignment(31L, 30L, 7L, 1, 0)));
        when(users.selectRolesByUserId(31L)).thenReturn(List.of(ordinary));
	        when(tenantRoles.selectTenantSuperRole(new TenantId(7L))).thenReturn(null);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(UserBO.class), eq(UserVO.class))).thenReturn(vo);
            assertNotNull(service.detailView(delegatedActor, 31L));
        }
        assertEquals(List.of(ordinary), fallback.getRoles());

        UserBO invalidDelegation = user(32L, "invalid-delegation");
        invalidDelegation.setExtInfo("{\"switchMode\":\"PARENT_SUPER_ADMIN\"}");
        RoleBO wrongTenantRole = role(40L, 8L, "tenant_super");
	        when(users.isUserInScope(32L, new TenantId(7L))).thenReturn(true);
        when(users.selectById(32L)).thenReturn(invalidDelegation);
        when(assignments.selectByUserId(32L)).thenReturn(List.of(assignment(32L, 30L, 7L, 1, 0)));
        when(users.selectRolesByUserId(32L)).thenReturn(List.of(ordinary));
	        when(tenantRoles.selectTenantSuperRole(new TenantId(7L))).thenReturn(wrongTenantRole);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(UserBO.class), eq(UserVO.class))).thenReturn(vo);
            assertNotNull(service.detailView(delegatedActor, 32L));
        }
        assertEquals(List.of(ordinary), invalidDelegation.getRoles());

        UserBO assigned = user(33L, "assigned");
        assigned.setExtInfo("{\"switchMode\":\"PARENT_SUPER_ADMIN\",\"currentRole\":{\"roleId\":30,\"roleKey\":\"teacher\"}}");
	        when(users.isUserInScope(33L, new TenantId(7L))).thenReturn(true);
        when(users.selectById(33L)).thenReturn(assigned);
        when(assignments.selectByUserId(33L)).thenReturn(List.of(assignment(33L, 30L, 7L, 1, 0)));
        when(users.selectRolesByUserId(33L)).thenReturn(List.of(ordinary));
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(UserBO.class), eq(UserVO.class))).thenReturn(vo);
            assertNotNull(service.detailView(delegatedActor, 33L));
        }
	        verify(tenantRoles, never()).selectTenantSuperRole(new TenantId(33L));
        assertEquals(ordinary, assigned.getCurrentRole());
    }

    @Test
    void coversDelegatedManagementFallbackAndTenantManageabilityGuards() {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRepository tenants = mock(TenantRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        UserServiceImpl service = new UserServiceImpl(users, roles, assignments, tenants, tenantRoles,
                mock(MenuService.class));

        ActorContext delegated = new ActorContext(new TenantId(7L), new UserId(11L), null,
                null, new TenantId(1L), "PARENT_SUPER_ADMIN", false);
	        when(users.isUserInScope(90L, new TenantId(7L))).thenReturn(false);
	        when(users.isUserInScope(90L, new TenantId(1L))).thenReturn(true);
        when(tenants.selectDescendantIds(new TenantId(7L))).thenReturn(List.of(7L));
        when(assignments.selectByUserId(90L)).thenReturn(List.of());
        assertTrue(service.getTenantAssignments(delegated, 90L).isEmpty());

        ActorContext noHome = new ActorContext(new TenantId(7L), new UserId(11L), null,
                null, null, "PARENT_SUPER_ADMIN", false);
        assertTrue(service.getTenantAssignments(noHome, 90L).isEmpty());
        assertTrue(service.getTenantAssignments(delegated, null).isEmpty());

        // Non-platform users cannot mutate a different tenant even when the
        // requested assignment is otherwise well formed.
        UserTenantRoleRequest child = new UserTenantRoleRequest();
        child.setTenantId(8L); child.setRoleId(31L);
        assertFalse(service.replaceTenantAssignments(delegated, 90L, List.of(child)));
    }

    @Test
    void preservesExplicitUpdateFieldsAndHandlesUpdateConflict() {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRepository tenants = mock(TenantRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        UserServiceImpl service = new UserServiceImpl(users, roles, assignments, tenants, tenantRoles,
                mock(MenuService.class));
        UserBO existing = user(40L, "existing");
        UserBO patch = user(null, "new-name");
        patch.setPassword("new-password");
        patch.setNickName("New"); patch.setGender("F"); patch.setAvatar("avatar");
        patch.setAddress("address"); patch.setEmail("new@example.test"); patch.setPhone("123");
        patch.setRemark("remark"); patch.setStatus(1); patch.setDelFlag(0); patch.setExtInfo("{}");
        UserRequest request = new UserRequest();
	        when(users.isUserInScope(40L, new TenantId(7L))).thenReturn(true, true);
        when(users.selectById(40L)).thenReturn(existing);
        when(users.update(any(UserBO.class))).thenReturn(true);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(request, UserBO.class)).thenReturn(patch);
            assertTrue(service.updateUser(ACTOR, 40L, request, null, 7L));
            assertEquals("new-name", patch.getUsername());
            assertEquals("New", patch.getNickName());
            assertEquals("F", patch.getGender());
            assertEquals("new@example.test", patch.getEmail());
        }

        when(users.update(any(UserBO.class))).thenReturn(false);
        UserRequest conflictRequest = new UserRequest();
        UserBO conflictPatch = user(null, "conflict");
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(conflictRequest, UserBO.class)).thenReturn(conflictPatch);
            assertFalse(service.updateUser(ACTOR, 40L, conflictRequest, new Long[]{30L}, 7L));
        }
        verify(assignments, never()).insert(any());

        // A patch containing omitted fields must preserve every persisted
        // value instead of overwriting it with null/blank data.
        UserBO completeExisting = user(41L, "existing-41");
        completeExisting.setPassword("encoded");
        completeExisting.setNickName("Nick"); completeExisting.setGender("F");
        completeExisting.setAvatar("avatar"); completeExisting.setAddress("address");
        completeExisting.setEmail("mail@example.test"); completeExisting.setPhone("123");
        completeExisting.setRemark("remark"); completeExisting.setExtInfo("{}");
        UserBO sparsePatch = new UserBO(); sparsePatch.setPassword(" ");
	        when(users.isUserInScope(41L, new TenantId(7L))).thenReturn(true);
        when(users.selectById(41L)).thenReturn(completeExisting);
        when(users.update(any(UserBO.class))).thenReturn(true);
        UserRequest sparseRequest = new UserRequest();
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(sparseRequest, UserBO.class)).thenReturn(sparsePatch);
            assertTrue(service.updateUser(ACTOR, 41L, sparseRequest, null, 7L));
        }
        assertEquals("existing-41", sparsePatch.getUsername());
        assertEquals("Nick", sparsePatch.getNickName());
        assertEquals("encoded", sparsePatch.getPassword());
    }

    @Test
    void filtersInactiveAssignmentsAndResolvesOnlyCurrentTenantRole() {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRepository tenants = mock(TenantRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        UserServiceImpl service = new UserServiceImpl(users, roles, assignments, tenants, tenantRoles,
                mock(MenuService.class));
        UserBO first = user(50L, "first");
        UserBO noId = user(null, "ignored");
        when(users.selectPage(eq(ACTOR.tenantId()), any(), any(), isNull())).thenReturn(Pair.of(2L, List.of(first, noId)));
        UserScopeRoleBO current = assignment(50L, 30L, 7L, 1, 0);
        UserScopeRoleBO nullStatus = assignment(50L, 31L, 7L, null, 0);
        UserScopeRoleBO deleted = assignment(50L, 32L, 7L, 1, 1);
        UserScopeRoleBO disabled = assignment(50L, 33L, 7L, 0, 0);
        UserScopeRoleBO otherTenant = assignment(50L, 34L, 8L, 1, 0);
        when(assignments.selectByUserIds(List.of(50L))).thenReturn(List.of(current, nullStatus, deleted, disabled, otherTenant));
        UserVO firstVo = new UserVO(); firstVo.setId(50L);
        UserVO noIdVo = new UserVO();
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(UserVO.class))).thenReturn(List.of(firstVo, noIdVo));
            PageData<UserVO> page = service.getUserList(ACTOR, null, 1, 10);
            assertEquals(2L, page.getTotal());
            assertEquals(List.of(30L), page.getItems().get(0).getRoleIds());
            assertTrue(page.getItems().get(1).getRoleIds().isEmpty());
        }

        UserBO emptyExt = user(51L, "empty-ext");
        emptyExt.setExtInfo(null);
	        when(users.isUserInScope(51L, new TenantId(7L))).thenReturn(true);
        when(users.selectById(51L)).thenReturn(emptyExt);
        when(assignments.selectByUserId(51L)).thenReturn(List.of());
        when(users.selectRolesByUserId(51L)).thenReturn(List.of());
        UserVO emptyVo = new UserVO();
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(UserBO.class), eq(UserVO.class))).thenReturn(emptyVo);
            assertNotNull(service.detailView(ACTOR, 51L));
        }
    }

    @Test
    void exercisesDelegatedRoleVariantsTenantAssignmentFiltersAndNullTargets() {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRepository tenants = mock(TenantRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        MenuService menu = mock(MenuService.class);
        UserServiceImpl service = new UserServiceImpl(users, roles, assignments, tenants, tenantRoles, menu);

        UserBO selectedRoleUser = user(60L, "selected");
        selectedRoleUser.setExtInfo("{\"currentRole\":{\"roleId\":999,\"roleKey\":\"teacher\"}}");
        RoleBO ordinary = role(30L, 7L, "teacher");
	        when(users.isUserInScope(60L, new TenantId(7L))).thenReturn(true);
        when(users.selectById(60L)).thenReturn(selectedRoleUser);
        when(assignments.selectByUserId(60L)).thenReturn(List.of(assignment(60L, 30L, 7L, 1, null)));
        when(users.selectRolesByUserId(60L)).thenReturn(List.of(ordinary));
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(UserBO.class), eq(UserVO.class))).thenReturn(new UserVO());
            assertNotNull(service.detailView(ACTOR, 60L));
        }
        assertEquals(ordinary, selectedRoleUser.getCurrentRole());

        ActorContext delegated = new ActorContext(new TenantId(7), new UserId(11), null,
                "tenant_super", new TenantId(7), "PARENT_SUPER_ADMIN", true);
        UserBO delegatedUser = user(61L, "delegated");
        delegatedUser.setExtInfo("{\"switchMode\":\"PARENT_SUPER_ADMIN\"}");
        RoleBO superRole = role(99L, 7L, "super");
	        when(users.isUserInScope(61L, new TenantId(7L))).thenReturn(true);
        when(users.selectById(61L)).thenReturn(delegatedUser);
        when(assignments.selectByUserId(61L)).thenReturn(List.of());
	        when(tenantRoles.selectTenantSuperRole(new TenantId(7L))).thenReturn(superRole);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(superRole, RoleBO.class)).thenReturn(superRole);
            mapper.when(() -> MapstructUtils.convert(any(UserBO.class), eq(UserVO.class))).thenReturn(new UserVO());
            assertNotNull(service.detailView(delegated, 61L));
        }
        assertEquals("super", delegatedUser.getCurrentRole().getCode());

        TenantBO active = tenant(7L, 1, null);
        TenantBO deleted = tenant(10L, 1, 1);
        RoleBO activeRole = role(30L, 7L, "teacher");
        RoleBO disabledRole = role(32L, 7L, "disabled"); disabledRole.setStatus(0);
        RoleBO roleWithoutStatus = role(31L, 7L, "unknown"); roleWithoutStatus.setStatus(null);
	        when(users.isUserInScope(70L, new TenantId(7L))).thenReturn(true);
        when(tenants.selectDescendantIds(new TenantId(7L))).thenReturn(List.of(7L, 8L, 9L, 10L));
        when(assignments.selectByUserId(70L)).thenReturn(List.of(
                assignment(70L, 30L, 7L, null, null),
                assignment(70L, 31L, 8L, 1, 0),
                assignment(70L, 32L, 9L, 1, 0),
                assignment(70L, 33L, 10L, 1, 0),
                assignment(70L, 34L, 99L, 1, 0),
                assignment(70L, 35L, 7L, 0, 0)));
	        when(tenantRoles.selectTenantById(new TenantId(7L))).thenReturn(active);
	        when(tenantRoles.selectTenantById(new TenantId(8L))).thenReturn(active);
	        when(tenantRoles.selectTenantById(new TenantId(9L))).thenReturn(active);
	        when(tenantRoles.selectTenantById(new TenantId(10L))).thenReturn(deleted);
        when(tenantRoles.selectRoleById(30L)).thenReturn(activeRole);
        when(tenantRoles.selectRoleById(31L)).thenReturn(roleWithoutStatus);
        when(tenantRoles.selectRoleById(32L)).thenReturn(disabledRole);
        when(tenantRoles.selectRoleById(33L)).thenReturn(activeRole);
        assertEquals(1, service.getTenantAssignments(ACTOR, 70L).size());

	        when(users.isUserInScope(80L, new TenantId(7L))).thenReturn(true);
        assertTrue(service.replaceTenantAssignments(ACTOR, 80L, null));
        assertTrue(service.replaceTenantAssignments(ACTOR, 80L,
                java.util.Arrays.asList(null, new UserTenantRoleRequest())));

        UserRequest request = new UserRequest();
        UserBO created = user(81L, "created");
        when(users.insert(any(UserBO.class))).thenReturn(created);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(request, UserBO.class)).thenReturn(user(null, "created"));
            assertThrows(IllegalArgumentException.class,
                    () -> service.createUser(ACTOR, request, new Long[]{30L}, 8L));
        }
    }

    private static UserBO user(Long id, String username) { UserBO value = new UserBO(); value.setId(id); value.setUsername(username); value.setStatus(1); value.setDelFlag(0); return value; }
    private static TenantBO tenant(Long id, Integer status, Integer delFlag) { TenantBO value = new TenantBO(); value.setId(id); value.setStatus(status); value.setDelFlag(delFlag); value.setName("tenant-" + id); return value; }
    private static RoleBO role(Long id, Long tenant, String code) { RoleBO value = new RoleBO(); value.setId(id); value.setTenantId(tenant); value.setCode(code); value.setName(code); value.setStatus(1); value.setDelFlag(0); return value; }
    private static UserScopeRoleBO assignment(Long user, Long role, Long tenant, Integer status, Integer delFlag) { UserScopeRoleBO value = new UserScopeRoleBO(); value.setUserId(user); value.setRoleId(role); value.setTenantId(tenant); value.setStatus(status); value.setDelFlag(delFlag); return value; }
}
