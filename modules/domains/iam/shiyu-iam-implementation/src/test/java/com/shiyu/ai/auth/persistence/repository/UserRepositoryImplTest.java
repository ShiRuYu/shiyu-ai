package com.shiyu.ai.auth.persistence.repository;

import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.auth.domain.model.UserBO;
import com.shiyu.ai.auth.persistence.dataobject.RoleDO;
import com.shiyu.ai.auth.persistence.dataobject.UserDO;
import com.shiyu.ai.auth.persistence.mapper.RoleMapper;
import com.shiyu.ai.auth.persistence.mapper.UserMapper;
import com.shiyu.ai.auth.persistence.mapper.UserScopeRoleMapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserRepositoryImplTest {
    private UserMapper users;
    private UserScopeRoleMapper assignments;
    private RoleMapper roles;
    private UserRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        users = mock(UserMapper.class);
        assignments = mock(UserScopeRoleMapper.class);
        roles = mock(RoleMapper.class);
        repository = new UserRepositoryImpl();
        ReflectionTestUtils.setField(repository, "userMapper", users);
        ReflectionTestUtils.setField(repository, "userScopeRoleMapper", assignments);
        ReflectionTestUtils.setField(repository, "roleMapper", roles);
    }

    @Test
    void rejectsMissingTenantForPaginationAndMissingScopeArguments() {
        assertThrows(IllegalArgumentException.class, () -> repository.selectPage(null, 1, 10, null));
        assertFalse(repository.isUserInScope(null, new TenantId(1L)));
        assertFalse(repository.isUserInScope(1L, null));
    }

    @Test
    void mapsQueriesAndPersistenceOperations() {
        UserDO userDO = new UserDO(); userDO.setId(7L); userDO.setUsername("alice");
        UserBO userBO = new UserBO(); userBO.setId(7L); userBO.setUsername("alice");
        RoleDO roleDO = new RoleDO(); roleDO.setId(3L); roleDO.setCode("user");
        RoleBO roleBO = new RoleBO(); roleBO.setId(3L); roleBO.setCode("user");
        when(users.selectOneByQuery(any())).thenReturn(userDO);
        when(users.selectOneById(7L)).thenReturn(userDO);
        when(users.selectListByQuery(any())).thenReturn(List.of(userDO));
        when(roles.selectListByQuery(any())).thenReturn(List.of(roleDO));
        when(users.selectCountByQuery(any())).thenReturn(1L);
        when(assignments.selectCountByQuery(any())).thenReturn(1L);
        when(users.update(any(UserDO.class))).thenReturn(1);
        when(users.deleteById(7L)).thenReturn(1);

        try (MockedStatic<MapstructUtils> mapstruct = mockStatic(MapstructUtils.class)) {
            mapstruct.when(() -> MapstructUtils.convert(userDO, UserBO.class)).thenReturn(userBO);
            mapstruct.when(() -> MapstructUtils.convert(any(UserDO.class), eq(UserBO.class))).thenReturn(userBO);
            mapstruct.when(() -> MapstructUtils.convert(anyList(), eq(UserBO.class))).thenReturn(List.of(userBO));
            mapstruct.when(() -> MapstructUtils.convert(anyList(), eq(RoleBO.class))).thenReturn(List.of(roleBO));
            mapstruct.when(() -> MapstructUtils.convert(any(UserBO.class), eq(UserDO.class))).thenReturn(userDO);

            assertSame(userBO, repository.selectByUsername("alice"));
            assertSame(userBO, repository.selectById(7L));
            assertSame(userBO, repository.selectByEmail("a@b.test"));
            assertSame(userBO, repository.selectActiveUserByUsername("alice"));
            assertEquals(List.of(roleBO), repository.selectRolesByUserId(7L));
            assertTrue(repository.update(userBO));
            assertTrue(repository.deleteById(7L));
            assertTrue(repository.isUserInScope(7L, new TenantId(9L)));
            verify(assignments).deleteByQuery(any());
        }
    }

    @Test
    void insertsAndPagesWithMappedValues() {
        UserBO user = new UserBO(); user.setUsername("new");
        UserDO row = new UserDO(); row.setId(42L);
        when(users.selectCountByQuery(any())).thenReturn(1L);
        when(users.selectListByQuery(any())).thenReturn(List.of(row));
        doAnswer(invocation -> { ((UserDO) invocation.getArgument(0)).setId(42L); return 1; }).when(users).insertSelective(any(UserDO.class));
        try (MockedStatic<MapstructUtils> mapstruct = mockStatic(MapstructUtils.class)) {
            mapstruct.when(() -> MapstructUtils.convert(any(UserDO.class), eq(UserBO.class))).thenReturn(user);
            mapstruct.when(() -> MapstructUtils.convert(anyList(), eq(UserBO.class))).thenReturn(List.of(user));
            mapstruct.when(() -> MapstructUtils.convert(any(UserBO.class), eq(UserDO.class))).thenReturn(row);

            assertEquals(42L, repository.insert(user).getId());
            Pair<Long, List<UserBO>> page = repository.selectPage(new TenantId(9L), 1, 10, "new");
            assertEquals(1L, page.getLeft());
            assertEquals(List.of(user), page.getRight());
            assertEquals(1L, repository.selectPage(new TenantId(9L), null, null, "").getLeft());
            assertEquals(1L, repository.selectPage(new TenantId(9L), null, null, null).getLeft());
        }
    }
}
