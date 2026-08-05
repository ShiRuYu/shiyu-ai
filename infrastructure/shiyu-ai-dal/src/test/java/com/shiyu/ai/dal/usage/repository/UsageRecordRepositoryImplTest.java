package com.shiyu.ai.dal.usage.repository;

import com.shiyu.ai.dal.usage.dataobject.UsageRecordDO;
import com.shiyu.ai.dal.usage.mapper.UsageRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsageRecordRepositoryImplTest {

    private UsageRecordMapper mapper;
    private UsageRecordRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        mapper = mock(UsageRecordMapper.class);
        repository = new UsageRecordRepositoryImpl(mapper);
    }

    @Test
    void aggregatesLlmRecordsWithoutDatabaseJsonFunctions() {
        when(mapper.selectLlmRecords()).thenReturn(List.of(
                record("LLM", "{\"platform\":\"openai\",\"model\":\"gpt\",\"totalTokens\":12,\"cost\":0.1}", 10),
                record("LLM", "{\"platform\":\"openai\",\"model\":\"gpt\",\"totalTokens\":8,\"cost\":0.2}", 30),
                record("LLM", "{\"platform\":\"deepseek\",\"model\":\"chat\",\"totalTokens\":30,\"cost\":0.3}", 5)
        ));

        List<Map<String, Object>> rows = repository.aggregateByModel();
        Map<String, Object> openAi = rows.stream()
                .filter(row -> "openai".equals(row.get("platform")))
                .findFirst()
                .orElseThrow();

        assertEquals(2L, openAi.get("call_count"));
        assertEquals(20L, openAi.get("total_tokens"));
        assertEquals(new BigDecimal("0.3"), openAi.get("total_cost"));
        assertEquals(20.0, openAi.get("avg_latency_ms"));
    }

    @Test
    void aggregatesPeriodsAndEmbeddingUsageInJava() {
        LocalDateTime now = LocalDateTime.now();
        when(mapper.selectLlmRecordsSince(any())).thenReturn(List.of(
                record("LLM", "{\"totalTokens\":7,\"cost\":0.2}", 14, now),
                record("LLM", "{\"totalTokens\":3,\"cost\":0.1}", 6, now)
        ));
        when(mapper.selectEmbeddingRecords()).thenReturn(List.of(
                record("EMBEDDING", "{\"estimatedTokens\":11,\"vectorCount\":2}", 8, now),
                record("EMBEDDING", "{\"estimatedTokens\":9,\"vectorCount\":3}", 12, now)
        ));

        Map<String, Object> llm = repository.aggregateLlmByDay(1).getFirst();
        Map<String, Object> embedding = repository.getEmbeddingOverview();

        assertEquals(2L, llm.get("call_count"));
        assertEquals(10L, llm.get("total_tokens"));
        assertEquals(new BigDecimal("0.3"), llm.get("total_cost"));
        assertEquals(10.0, llm.get("avg_latency_ms"));
        assertEquals(2L, embedding.get("total_calls"));
        assertEquals(20L, embedding.get("total_estimated_tokens"));
        assertEquals(5L, embedding.get("total_vectors"));
        assertEquals(10.0, embedding.get("avg_latency_ms"));
    }

    @Test
    void sumsTodayTokensForTheSpecifiedTenant() {
        when(mapper.selectLlmTodayByTenantId(eq(1L), any())).thenReturn(List.of(
                record("LLM", "{\"totalTokens\":4}", 1),
                record("LLM", "{\"totalTokens\":6}", 1)
        ));

        assertEquals(10L, repository.sumLlmTodayTokensByTenantId(1L));
        assertEquals(0L, repository.sumLlmTodayTokensByTenantId(null));
    }

    private static UsageRecordDO record(String usageType, String extInfo, long latencyMs) {
        return record(usageType, extInfo, latencyMs, LocalDateTime.now());
    }

    private static UsageRecordDO record(String usageType, String extInfo, long latencyMs, LocalDateTime createTime) {
        UsageRecordDO record = new UsageRecordDO();
        record.setUsageType(usageType);
        record.setExtInfo(extInfo);
        record.setLatencyMs(latencyMs);
        record.setCreateTime(createTime);
        return record;
    }
}
