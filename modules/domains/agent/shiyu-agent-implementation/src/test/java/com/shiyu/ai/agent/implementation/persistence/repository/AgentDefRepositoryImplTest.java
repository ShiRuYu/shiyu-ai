package com.shiyu.ai.agent.implementation.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.shiyu.ai.agent.implementation.persistence.mapper.AgentDefMapper;
import com.shiyu.ai.kernel.context.TenantId;

import org.junit.jupiter.api.Test;

class AgentDefRepositoryImplTest {
    @Test
    void countByTenantRequiresTenantAndAppliesMapperResult() {
        var mapper = mock(AgentDefMapper.class);
        when(mapper.selectCountByQuery(any())).thenReturn(3L);
        var repository = new AgentDefRepositoryImpl();
        setMapper(repository, mapper);
        assertEquals(3L, repository.countByTenantId(new TenantId(7L)));
        assertThrows(IllegalArgumentException.class, () -> repository.countByTenantId(null));
    }

    private static void setMapper(AgentDefRepositoryImpl target, AgentDefMapper mapper) {
        try {
            var field = AgentDefRepositoryImpl.class.getDeclaredField("agentDefMapper");
            field.setAccessible(true);
            field.set(target, mapper);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
