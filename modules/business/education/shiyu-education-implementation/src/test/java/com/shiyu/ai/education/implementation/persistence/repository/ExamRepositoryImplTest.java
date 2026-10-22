package com.shiyu.ai.education.implementation.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.education.implementation.persistence.mapper.ExamMapper;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ExamRepositoryImplTest {
    @Test
    void batchExamLookupUsesTenantAndDeletionFiltersAndSkipsEmptyIds() throws Exception {
        ExamMapper mapper = mock(ExamMapper.class);
        when(mapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of());
        ExamRepositoryImpl repository = new ExamRepositoryImpl();
        var mapperField = ExamRepositoryImpl.class.getDeclaredField("examMapper");
        mapperField.setAccessible(true);
        mapperField.set(repository, mapper);

        repository.selectByIds(new TenantId(9L), List.of(5L, 6L));
        repository.selectByIds(new TenantId(9L), List.of());

        ArgumentCaptor<QueryWrapper> queries = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper, times(1)).selectListByQuery(queries.capture());
        String sql = queries.getValue().toSQL().toLowerCase();
        assertTrue(sql.contains("tenant_id"));
        assertTrue(sql.contains("id"));
        assertTrue(sql.contains("del_flag"));
    }
}
