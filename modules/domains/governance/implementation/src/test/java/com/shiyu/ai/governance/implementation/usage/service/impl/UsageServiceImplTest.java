package com.shiyu.ai.governance.implementation.usage.service.impl;

import com.shiyu.ai.governance.implementation.usage.port.repository.UsageRecordRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsageServiceImplTest {

    private final UsageRecordRepository repository = mock(UsageRecordRepository.class);
    private final UsageServiceImpl service = new UsageServiceImpl(repository);

    @Test
    void delegatesAllUsageQueriesWithoutChangingTheRepositoryContract() {
        Map<String, Object> overview = Map.of("total_calls", 1L);
        List<Map<String, Object>> rows = List.of(Map.of("call_count", 1L));
        when(repository.getOverview()).thenReturn(overview);
        when(repository.aggregateByDay(1)).thenReturn(rows);
        when(repository.aggregateByWeek(2)).thenReturn(rows);
        when(repository.aggregateByMonth(3)).thenReturn(rows);
        when(repository.aggregateByModel()).thenReturn(rows);
        when(repository.aggregateLlmByDay(4)).thenReturn(rows);
        when(repository.aggregateLlmByWeek(5)).thenReturn(rows);
        when(repository.aggregateLlmByMonth(6)).thenReturn(rows);
        when(repository.getEmbeddingOverview()).thenReturn(overview);

        assertEquals(overview, service.overview());
        assertEquals(rows, service.byDay(1));
        assertEquals(rows, service.byWeek(2));
        assertEquals(rows, service.byMonth(3));
        assertEquals(rows, service.byModel());
        assertEquals(rows, service.llmByDay(4));
        assertEquals(rows, service.llmByWeek(5));
        assertEquals(rows, service.llmByMonth(6));
        assertEquals(overview, service.embeddingOverview());
        verify(repository).aggregateLlmByMonth(6);
    }
}
