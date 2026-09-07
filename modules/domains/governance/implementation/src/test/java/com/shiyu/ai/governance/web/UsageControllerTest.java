package com.shiyu.ai.governance.web;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.governance.implementation.usage.service.UsageService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsageControllerTest {

    private final UsageService service = mock(UsageService.class);
    private final UsageController controller = new UsageController(service);

    @Test
    void returnsSuccessfulResultsForEveryUsageQuery() {
        Map<String, Object> overview = Map.of("total_calls", 1L);
        List<Map<String, Object>> rows = List.of(Map.of("call_count", 1L));
        when(service.overview()).thenReturn(overview);
        when(service.byDay(7)).thenReturn(rows);
        when(service.byWeek(4)).thenReturn(rows);
        when(service.byMonth(6)).thenReturn(rows);
        when(service.byModel()).thenReturn(rows);
        when(service.llmByDay(7)).thenReturn(rows);
        when(service.llmByWeek(4)).thenReturn(rows);
        when(service.llmByMonth(6)).thenReturn(rows);
        when(service.embeddingOverview()).thenReturn(overview);

        assertTrue(controller.getOverview().isSuccess());
        assertEquals(overview, controller.getOverview().getData());
        assertEquals(rows, controller.aggregateByDay(7).getData());
        assertEquals(rows, controller.aggregateByWeek(4).getData());
        assertEquals(rows, controller.aggregateByMonth(6).getData());
        assertEquals(rows, controller.aggregateByModel().getData());
        assertEquals(rows, controller.aggregateLlmByDay(7).getData());
        assertEquals(rows, controller.aggregateLlmByWeek(4).getData());
        assertEquals(rows, controller.aggregateLlmByMonth(6).getData());
        assertEquals(overview, controller.getEmbeddingOverview().getData());
    }

    @Test
    void mapsEveryServiceFailureToTheEndpointSpecificSafeMessage() {
        RuntimeException failure = new RuntimeException("database password=secret");
        when(service.overview()).thenThrow(failure);
        when(service.byDay(1)).thenThrow(failure);
        when(service.byWeek(2)).thenThrow(failure);
        when(service.byMonth(3)).thenThrow(failure);
        when(service.byModel()).thenThrow(failure);
        when(service.llmByDay(4)).thenThrow(failure);
        when(service.llmByWeek(5)).thenThrow(failure);
        when(service.llmByMonth(6)).thenThrow(failure);
        when(service.embeddingOverview()).thenThrow(failure);

        assertFailure(controller.getOverview(), "获取用量概览失败");
        assertFailure(controller.aggregateByDay(1), "按日聚合查询失败");
        assertFailure(controller.aggregateByWeek(2), "按周聚合查询失败");
        assertFailure(controller.aggregateByMonth(3), "按月聚合查询失败");
        assertFailure(controller.aggregateByModel(), "按模型聚合查询失败");
        assertFailure(controller.aggregateLlmByDay(4), "LLM 按日聚合失败");
        assertFailure(controller.aggregateLlmByWeek(5), "LLM 按周聚合失败");
        assertFailure(controller.aggregateLlmByMonth(6), "LLM 按月聚合失败");
        assertFailure(controller.getEmbeddingOverview(), "获取 Embedding 用量概览失败");
    }

    private static void assertFailure(Result<?> result, String message) {
        assertFalse(result.isSuccess());
        assertEquals(message, result.getMessage());
    }
}
