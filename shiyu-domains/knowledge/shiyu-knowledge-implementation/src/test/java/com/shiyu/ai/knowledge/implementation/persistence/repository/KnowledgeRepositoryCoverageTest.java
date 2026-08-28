package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.paginate.Page;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.knowledge.domain.model.KnowledgeBO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDO;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeMapper;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class KnowledgeRepositoryCoverageTest {
    private static final TenantId TENANT = new TenantId(31L);

    @Test
    void exercisesTenantScopedQueriesPagingAndMutations() throws Exception {
        KnowledgeMapper mapper = mock(KnowledgeMapper.class);
        KnowledgeRepositoryImpl repository = new KnowledgeRepositoryImpl();
        Field field = KnowledgeRepositoryImpl.class.getDeclaredField("knowledgeMapper");
        field.setAccessible(true);
        field.set(repository, mapper);

        KnowledgeDO data = new KnowledgeDO();
        data.setId(8L);
        data.setTenantId(31L);
        KnowledgeDO other = new KnowledgeDO();
        other.setId(9L);
        other.setTenantId(31L);
        when(mapper.selectOneByQuery(any())).thenReturn(data);
        when(mapper.selectListByQuery(any())).thenReturn(List.of(data));
        when(mapper.selectCountByQuery(any())).thenReturn(1L);
        when(mapper.deleteByQuery(any())).thenReturn(1);
        when(mapper.updateByQuery(any(), any())).thenReturn(1);
        when(mapper.insert(any(KnowledgeDO.class))).thenAnswer(invocation -> {
            KnowledgeDO value = invocation.getArgument(0);
            value.setId(99L);
            return 1;
        });
        Page<KnowledgeDO> page = Page.of(1, 10, 1);
        page.setRecords(List.of(data));
        when(mapper.paginate(any(Number.class), any(Number.class), any(com.mybatisflex.core.query.QueryWrapper.class)))
                .thenReturn(page);

        KnowledgeBO converted = new KnowledgeBO();
        converted.setId(8L);
        KnowledgeBO source = new KnowledgeBO();
        source.setId(8L);
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(any(KnowledgeDO.class), eq(KnowledgeBO.class)))
                    .thenReturn(converted);
            conversions.when(() -> MapstructUtils.convert(any(KnowledgeBO.class), eq(KnowledgeDO.class)))
                    .thenAnswer(invocation -> new KnowledgeDO());
            conversions.when(() -> MapstructUtils.convert(any(List.class), eq(KnowledgeBO.class)))
                    .thenReturn(List.of(converted));

            assertNotNull(repository.findById(TENANT, 8L));
            assertNotNull(repository.findByCode(TENANT, "code"));
            assertEquals(1, repository.findAll(TENANT).size());
            assertEquals(1, repository.searchByName(TENANT, "math", 5).size());
            assertEquals(1, repository.page(TENANT, 0, 10).size());
            assertEquals(1, repository.page(TENANT, 0, 10, "MATH", "algebra").size());
            assertEquals(1, repository.count(TENANT));
            assertEquals(1, repository.count(TENANT, "MATH", "algebra"));

            assertEquals(1, repository.insert(TENANT, source));
            assertEquals(99L, source.getId());
            assertEquals(1, repository.update(TENANT, source));
            assertEquals(1, repository.deleteById(TENANT, 8L));
            assertTrue(repository.existsByCode(TENANT, "code"));
            assertTrue(repository.existsBySpaceAndCode(TENANT, 3L, "code"));
            assertEquals(1, repository.findBySpace(TENANT, 3L).size());
            assertEquals(1, repository.pageBySpace(TENANT, 3L, 1, 10, "math", "MATH").getTotal());
            assertEquals(1, repository.deleteByIdAndSpace(TENANT, 8L, 3L));
        }

        when(mapper.selectCountByQuery(any())).thenReturn(0L);
        when(mapper.selectOneByQuery(any())).thenReturn(null);
        assertFalse(repository.existsByCode(TENANT, "missing"));
        assertFalse(repository.existsBySpaceAndCode(TENANT, 3L, "missing"));
        assertEquals(0, repository.update(TENANT, source));

        KnowledgeBO noTenant = new KnowledgeBO();
        noTenant.setId(1L);
        noTenant.setTenantId(99L);
        assertThrows(IllegalArgumentException.class, () -> repository.update(TENANT, noTenant));
        assertThrows(IllegalArgumentException.class, () -> repository.findById(null, 1L));

        when(mapper.selectListByQuery(any())).thenReturn(List.of(data, other));
        repository.assignDefaultSpace(TENANT, 4L);
        verify(mapper, times(2)).update(any(KnowledgeDO.class));

        when(mapper.selectListByQuery(any())).thenReturn(List.of());
        repository.assignDefaultSpace(TENANT, 4L);
    }
}
