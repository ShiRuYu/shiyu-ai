package com.shiyu.ai.auth.persistence.repository;

import com.shiyu.ai.auth.persistence.dataobject.AuthCodeDO;
import com.shiyu.ai.auth.persistence.dataobject.RoleDO;
import com.shiyu.ai.auth.persistence.mapper.AuthCodeMapper;
import com.shiyu.ai.auth.persistence.mapper.RoleMapper;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthRepositoryImplTest {
    private AuthCodeMapper authCodes;
    private RoleMapper roles;
    private AuthRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        authCodes = mock(AuthCodeMapper.class);
        roles = mock(RoleMapper.class);
        repository = new AuthRepositoryImpl();
        ReflectionTestUtils.setField(repository, "authCodeMapper", authCodes);
        ReflectionTestUtils.setField(repository, "roleMapper", roles);
    }

    @Test
    void mapsRoleCodesAndAllPermissionQueryVariants() {
        RoleDO role = new RoleDO(); role.setId(3L); role.setCode("editor");
        when(roles.selectListByQuery(any())).thenReturn(List.of(role, role));
        assertEquals(List.of("editor"), repository.selectRoleCodesByUserId(new UserId(20L), new TenantId(10L)));

        AuthCodeDO read = code(1L, "read");
        AuthCodeDO duplicate = code(2L, "read");
        AuthCodeDO write = code(3L, "write");
        when(authCodes.selectListByQuery(any())).thenReturn(List.of(read, duplicate));
        assertEquals(List.of("read"), repository.selectCodesByUserIdAndRoleCode(new UserId(20L), new TenantId(10L), "editor"));
        assertEquals(List.of("read"), repository.selectCodesByRoleCodeAndTenant("tenant_super", new TenantId(10L)));
        assertEquals(List.of("read"), repository.selectCodesByUsername("alice", new TenantId(10L)));
        assertEquals(List.of("read"), repository.selectCodesByUserId(new UserId(20L), new TenantId(10L)));
        when(authCodes.selectListByQuery(any())).thenReturn(List.of(read, write));
        assertEquals(List.of("read", "write"), repository.selectCodesByRoleId(3L, new TenantId(10L)));
    }

    @Test
    void returnsEmptyListsWhenMappersReturnNoRows() {
        when(roles.selectListByQuery(any())).thenReturn(List.of());
        when(authCodes.selectListByQuery(any())).thenReturn(List.of());
        assertEquals(List.of(), repository.selectRoleCodesByUserId(new UserId(1L), new TenantId(1L)));
        assertEquals(List.of(), repository.selectCodesByUserIdAndRoleCode(new UserId(1L), new TenantId(1L), "user"));
        assertEquals(List.of(), repository.selectCodesByRoleCodeAndTenant("user", new TenantId(1L)));
        assertEquals(List.of(), repository.selectCodesByUsername("none", new TenantId(1L)));
        assertThrows(NullPointerException.class, () -> repository.selectCodesByUserId(new UserId(1L), null));
        assertEquals(List.of(), repository.selectCodesByRoleId(1L, new TenantId(1L)));
        assertThrows(NullPointerException.class, () -> repository.selectCodesByUsername("none", null));
        assertThrows(NullPointerException.class, () -> repository.selectCodesByRoleId(1L, null));
        assertThrows(IllegalArgumentException.class,
                () -> repository.selectCodesByUserIdAndRoleCode(new UserId(1L), new TenantId(1L), " "));
        assertThrows(IllegalArgumentException.class,
                () -> repository.selectCodesByRoleCodeAndTenant("", new TenantId(1L)));
    }

    private static AuthCodeDO code(Long id, String value) {
        AuthCodeDO code = new AuthCodeDO(); code.setId(id); code.setCode(value); return code;
    }
}
