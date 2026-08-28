package com.shiyu.ai.auth.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.auth.domain.model.*;
import com.shiyu.ai.auth.persistence.dataobject.*;
import com.shiyu.ai.auth.persistence.mapper.*;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class AuthorizationRepositoriesTest {
    private static final TenantId TENANT = new TenantId(21);

    @Test
    void coversAuthorizationCodeTenantAndRoleAssignments() throws Exception {
        AuthCodeMapper codes = mock(AuthCodeMapper.class);
        TenantAuthCodeMapper tenantCodes = mock(TenantAuthCodeMapper.class);
        RoleScopeAuthCodeMapper roleCodes = mock(RoleScopeAuthCodeMapper.class);
        AuthCodeRepositoryImpl repository = new AuthCodeRepositoryImpl();
        inject(repository, "authCodeMapper", codes); inject(repository, "tenantAuthCodeMapper", tenantCodes); inject(repository, "roleScopeAuthCodeMapper", roleCodes);
        when(tenantCodes.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of());
        assertTrue(repository.selectByTenantId(TENANT).isEmpty());
        assertTrue(repository.selectAvailableByIds(List.of(), TENANT).isEmpty());
        assertThrows(NullPointerException.class, () -> repository.selectAvailableByIds(List.of(), null));
        when(roleCodes.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of());
        assertTrue(repository.selectByRoleIdAndTenantId(2L, TENANT).isEmpty());
        TenantAuthCodeDO link = new TenantAuthCodeDO(); link.setAuthCodeId(4L);
        when(tenantCodes.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(link));
        when(codes.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(new AuthCodeDO()));
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(any(List.class), eq(AuthCodeBO.class))).thenReturn(List.of(new AuthCodeBO()));
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(AuthCodeBO.class))).thenReturn(new AuthCodeBO());
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(AuthCodeDO.class))).thenReturn(new AuthCodeDO());
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(TenantAuthCodeDO.class))).thenReturn(new TenantAuthCodeDO());
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(RoleScopeAuthCodeDO.class))).thenReturn(new RoleScopeAuthCodeDO());
            assertEquals(1, repository.selectByTenantId(TENANT).size());
            assertEquals(1, repository.selectAvailableByIds(List.of(4L), TENANT).size());
            when(codes.selectOneById(4L)).thenReturn(new AuthCodeDO()); assertNotNull(repository.selectById(4L));
            AuthCodeBO code = new AuthCodeBO(); assertNotNull(repository.insert(code)); repository.update(code);
            when(codes.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L, 0L);
            assertTrue(repository.existsByCode("read", null)); assertFalse(repository.existsByCode("read", 2L));
            when(tenantCodes.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L, 0L);
            assertTrue(repository.isAvailable(4L, TENANT)); assertEquals(0L, repository.countActiveTenantLinks(4L));
            when(roleCodes.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L);
            assertTrue(repository.hasRoleAssignments(4L));
            repository.insertTenantCode(new TenantAuthCodeBO()); repository.deleteTenantCode(TENANT, 4L);
            repository.insertRoleAssignments(List.of(new RoleScopeAuthCodeBO())); repository.insertRoleAssignments(List.of());
            repository.deleteRoleAssignments(2L, TENANT, null); repository.deleteRoleAssignments(2L, TENANT, 4L);
        }
    }

    @Test
    void coversDictionaryTenantScopedCrudAndValidation() throws Exception {
        DictMapper mapper = mock(DictMapper.class); DictRepositoryImpl repository = new DictRepositoryImpl(); inject(repository, "dictMapper", mapper);
        when(mapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(2L);
        when(mapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(new DictDO()));
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(any(List.class), eq(DictBO.class))).thenReturn(List.of(new DictBO()));
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(DictBO.class))).thenReturn(new DictBO());
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(DictDO.class))).thenReturn(new DictDO());
            assertEquals(2L, repository.selectPage(TENANT, 1, 10).getLeft());
            assertEquals(1, repository.selectPage(TENANT, null, null).getRight().size());
            assertEquals(1, repository.selectAll(TENANT).size()); assertEquals(1, repository.selectByDictType(TENANT, "role").size());
            when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(new DictDO()); assertNotNull(repository.selectById(TENANT, 1L));
            DictBO dict = new DictBO(); when(mapper.insertSelective(any(DictDO.class))).thenAnswer(i -> { ((DictDO) i.getArgument(0)).setId(8L); return 1; });
            assertEquals(8L, repository.create(dict).getId()); repository.update(dict); assertNull(repository.update(null));
            dict.setId(null); assertNull(repository.update(dict));
            repository.deleteById(TENANT, 8L); repository.deleteByIds(TENANT, List.of(8L, 9L));
        }
        assertThrows(IllegalArgumentException.class, () -> repository.selectAll(null));
    }

    private static void inject(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name); field.setAccessible(true); field.set(target, value);
    }
}
