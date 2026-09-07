package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
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
class KnowledgeRepositoryTenantTest {
    private static final TenantId TENANT = new TenantId(31);

    @Test
    void scopesKnowledgeReadsWritesAndTenantMismatch() throws Exception {
        KnowledgeMapper mapper = mock(KnowledgeMapper.class); KnowledgeRepositoryImpl repository = new KnowledgeRepositoryImpl(); inject(repository, "knowledgeMapper", mapper);
        KnowledgeDO data = new KnowledgeDO(); data.setId(4L); data.setTenantId(31L);
        when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(data);
        when(mapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(data));
        when(mapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L);
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(any(List.class), eq(KnowledgeBO.class))).thenReturn(List.of(new KnowledgeBO()));
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(KnowledgeBO.class))).thenReturn(new KnowledgeBO());
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(KnowledgeDO.class))).thenReturn(new KnowledgeDO());
            assertNotNull(repository.findById(TENANT, 4L)); assertNotNull(repository.findByCode(TENANT, "kb"));
            assertEquals(1, repository.findAll(TENANT).size()); assertEquals(1, repository.searchByName(TENANT, "math", 5).size());
            assertEquals(1, repository.page(TENANT, 0, 5, "math", "math").size()); assertEquals(1L, repository.count(TENANT, "math", "math"));
            KnowledgeBO bo = new KnowledgeBO(); bo.setTenantId(31L);
            when(mapper.insert(any(KnowledgeDO.class))).thenAnswer(i -> { ((KnowledgeDO) i.getArgument(0)).setId(9L); return 1; });
            assertEquals(1, repository.insert(TENANT, bo));
            when(mapper.updateByQuery(any(KnowledgeDO.class), any(QueryWrapper.class))).thenReturn(1);
            assertEquals(1, repository.update(TENANT, bo)); assertTrue(repository.existsByCode(TENANT, "kb"));
            assertTrue(repository.existsBySpaceAndCode(TENANT, 2L, "kb")); assertEquals(1, repository.findBySpace(TENANT, 2L).size());
            when(mapper.deleteByQuery(any(QueryWrapper.class))).thenReturn(1);
            assertEquals(1, repository.deleteById(TENANT, 4L)); assertEquals(1, repository.deleteByIdAndSpace(TENANT, 4L, 2L));
            repository.assignDefaultSpace(TENANT, 2L);
            when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);
            assertEquals(0, repository.update(TENANT, bo));
        }
        assertThrows(IllegalArgumentException.class, () -> repository.findAll(null));
        KnowledgeBO wrong = new KnowledgeBO(); wrong.setTenantId(99L);
        assertThrows(IllegalArgumentException.class, () -> repository.update(TENANT, wrong));
    }

    private static void inject(Object target, String name, Object value) throws Exception { Field field = target.getClass().getDeclaredField(name); field.setAccessible(true); field.set(target, value); }
}
