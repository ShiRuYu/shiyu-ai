package com.shiyu.ai.agent.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.domain.model.IntentDefBO;
import com.shiyu.ai.agent.persistence.dataobject.IntentDefDO;
import com.shiyu.ai.agent.persistence.mapper.IntentDefMapper;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class IntentDefRepositoryImplTest {
    private static final TenantId TENANT = new TenantId(7L);

    @Test
    void queriesConvertJsonFieldsAndApplyPagingFilters() {
        IntentDefMapper mapper = mock(IntentDefMapper.class);
        IntentDefRepositoryImpl repository = repository(mapper);
        IntentDefDO row = row(11L);
        when(mapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L);
        when(mapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(row));

        var page = repository.selectPage(TENANT, 2, 10, "agent", "name", "code", "category");
        assertEquals(1L, page.getLeft());
        assertEquals("WEATHER", page.getRight().getFirst().getCode());
        assertEquals(List.of("rain"), page.getRight().getFirst().getExamples());
        assertTrue(page.getRight().getFirst().getRequireSlotFilling());
        assertEquals("city name", page.getRight().getFirst().getSlots().get("city"));
        assertEquals("query", page.getRight().getFirst().getParameterMapping().get("city"));
        assertEquals("Beijing", page.getRight().getFirst().getSlotDefaults().get("city"));

        repository.selectPage(TENANT, null, null, null, null, null, null);
        verify(mapper, times(2)).selectCountByQuery(any(QueryWrapper.class));
        verify(mapper, times(2)).selectListByQuery(any(QueryWrapper.class));

        assertEquals(1, repository.selectByAgentId(TENANT, "agent").size());
        assertEquals(1, repository.selectByCategory(TENANT, "agent", "conversation").size());
        when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(row);
        assertEquals(11L, repository.selectById(TENANT, 11L).getId());
        when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);
        assertNull(repository.selectById(TENANT, 99L));
    }

    @Test
    void createsUpdatesDeletesAndListsOptionsWithTenantScope() {
        IntentDefMapper mapper = mock(IntentDefMapper.class);
        IntentDefRepositoryImpl repository = repository(mapper);
        doAnswer(invocation -> {
            IntentDefDO inserted = invocation.getArgument(0);
            inserted.setId(22L);
            return 1;
        }).when(mapper).insertSelective(any(IntentDefDO.class));
        IntentDefBO bo = businessObject();
        IntentDefBO created = repository.create(TENANT, bo);
        assertEquals(22L, created.getId());
        assertEquals(7L, created.getTenantId());
        verify(mapper).insertSelective(any(IntentDefDO.class));

        created.setId(22L);
        assertSame(created, repository.update(TENANT, created));
        verify(mapper).updateByQuery(any(IntentDefDO.class), any(QueryWrapper.class));
        repository.deleteById(TENANT, 22L);
        repository.deleteByIds(TENANT, List.of(22L, 23L));
        verify(mapper, times(3)).deleteByQuery(any(QueryWrapper.class));

        IntentDefDO option = row(31L);
        when(mapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(option));
        var options = repository.selectAllOptions(TENANT);
        assertEquals(1, options.size());
        assertEquals(31L, options.getFirst().getId());
        assertEquals("WEATHER", options.getFirst().getCode());
    }

    @Test
    void rejectsMissingTenantAndInvalidCommands() {
        IntentDefRepositoryImpl repository = repository(mock(IntentDefMapper.class));
        assertThrows(IllegalArgumentException.class, () -> repository.selectPage(null, 1, 10, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> repository.selectByAgentId(null, "a"));
        assertThrows(IllegalArgumentException.class, () -> repository.selectByCategory(null, "a", "c"));
        assertThrows(IllegalArgumentException.class, () -> repository.selectById(null, 1L));
        assertThrows(IllegalArgumentException.class, () -> repository.create(null, businessObject()));
        assertThrows(IllegalArgumentException.class, () -> repository.create(TENANT, null));
        assertThrows(IllegalArgumentException.class, () -> repository.update(TENANT, null));
        IntentDefBO missingId = businessObject();
        assertThrows(IllegalArgumentException.class, () -> repository.update(TENANT, missingId));
        assertThrows(IllegalArgumentException.class, () -> repository.deleteById(null, 1L));
        assertThrows(IllegalArgumentException.class, () -> repository.deleteByIds(null, List.of(1L)));
        assertThrows(IllegalArgumentException.class, () -> repository.selectAllOptions(null));
    }

    @Test
    void handlesSparseDefinitionsAndNullJsonColumns() {
        IntentDefMapper mapper = mock(IntentDefMapper.class);
        IntentDefRepositoryImpl repository = repository(mapper);
        doAnswer(invocation -> {
            IntentDefDO inserted = invocation.getArgument(0);
            inserted.setId(44L);
            return 1;
        }).when(mapper).insertSelective(any(IntentDefDO.class));
        IntentDefBO sparse = new IntentDefBO();
        IntentDefBO created = repository.create(TENANT, sparse);
        assertEquals(44L, created.getId());
        created.setId(44L);
        repository.update(TENANT, created);

        IntentDefDO nullJson = new IntentDefDO();
        nullJson.setId(45L);
        nullJson.setEnabled(null);
        nullJson.setRequireSlotFilling(null);
        nullJson.setExamples(null);
        nullJson.setSlots(null);
        nullJson.setParameterMapping(null);
        nullJson.setSlotDefaults(null);
        when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(nullJson);
        IntentDefBO loaded = repository.selectById(TENANT, 45L);
        assertNotNull(loaded);
        assertNull(loaded.getEnabled());
        assertNull(loaded.getSlots());
    }

    private static IntentDefRepositoryImpl repository(IntentDefMapper mapper) {
        IntentDefRepositoryImpl repository = new IntentDefRepositoryImpl();
        try {
            var field = IntentDefRepositoryImpl.class.getDeclaredField("intentDefMapper");
            field.setAccessible(true);
            field.set(repository, mapper);
            return repository;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static IntentDefDO row(long id) {
        IntentDefDO row = new IntentDefDO();
        row.setId(id); row.setTenantId(7L); row.setAgentId("agent"); row.setCode("WEATHER");
        row.setName("Weather"); row.setDescription("weather lookup"); row.setCategory("conversation");
        row.setPriority(1); row.setConfidenceThreshold(0.8); row.setExamples("[\"rain\"]");
        row.setTargetNode("weather-node"); row.setRequireSlotFilling("1"); row.setSlots("{\"city\":\"city name\"}");
        row.setParameterMapping("{\"city\":\"query\"}"); row.setSlotDefaults("{\"city\":\"Beijing\"}");
        row.setEnabled("1"); row.setStatus(1); row.setDelFlag(0);
        return row;
    }

    private static IntentDefBO businessObject() {
        IntentDefBO bo = new IntentDefBO();
        bo.setAgentId("agent"); bo.setCode("WEATHER"); bo.setName("Weather"); bo.setDescription("weather lookup");
        bo.setCategory("conversation"); bo.setPriority(1); bo.setConfidenceThreshold(0.8);
        bo.setExamples(List.of("rain")); bo.setTargetNode("weather-node"); bo.setRequireSlotFilling(true);
        bo.setSlots(Map.of("city", "city name")); bo.setParameterMapping(Map.of("city", "query"));
        bo.setSlotDefaults(Map.of("city", "Beijing")); bo.setEnabled(true);
        return bo;
    }
}
