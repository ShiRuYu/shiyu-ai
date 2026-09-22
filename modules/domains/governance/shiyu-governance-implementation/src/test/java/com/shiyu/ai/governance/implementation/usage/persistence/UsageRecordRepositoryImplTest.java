package com.shiyu.ai.governance.implementation.usage.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.governance.implementation.usage.domain.model.UsageRecordBO;
import com.shiyu.ai.governance.implementation.usage.persistence.dataobject.UsageRecordDO;
import com.shiyu.ai.governance.implementation.usage.persistence.mapper.UsageRecordMapper;
import com.shiyu.ai.governance.implementation.usage.persistence.repository.UsageRecordRepositoryImpl;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.model.contract.api.ModelCatalogPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 验证 用量 Record Repository Impl 相关功能、边界条件、异常路径和协作行为。
 */
class UsageRecordRepositoryImplTest {

    private UsageRecordMapper mapper;
    private ModelCatalogPort modelCatalog;
    private UsageRecordRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        TenantScope.set(new TenantId(9L));
        mapper = mock(UsageRecordMapper.class);
        modelCatalog = mock(ModelCatalogPort.class);
        when(modelCatalog.countEnabledPlatforms()).thenReturn(4L);
        when(modelCatalog.countEnabledModels()).thenReturn(9L);
        repository = new UsageRecordRepositoryImpl(mapper, modelCatalog);
    }

    @AfterEach
    void clearScope() {
        TenantScope.clear();
    }

    @Test
    void aggregatesLlmRecordsWithoutDatabaseJsonFunctions() {
        when(mapper.selectLlmRecords(anyLong()))
                .thenReturn(
                        List.of(
                                record(
                                        "LLM",
                                        "{\"platform\":\"openai\",\"model\":\"gpt\",\"totalTokens\":12,\"cost\":0.1}",
                                        10),
                                record(
                                        "LLM",
                                        "{\"platform\":\"openai\",\"model\":\"gpt\",\"totalTokens\":8,\"cost\":0.2}",
                                        30),
                                record(
                                        "LLM",
                                        "{\"platform\":\"deepseek\",\"model\":\"chat\",\"totalTokens\":30,\"cost\":0.3}",
                                        5)));

        List<Map<String, Object>> rows = repository.aggregateByModel();
        Map<String, Object> openAi =
                rows.stream()
                        .filter(row -> "openai".equals(row.get("platform")))
                        .findFirst()
                        .orElseThrow();

        assertEquals(2L, openAi.get("call_count"));
        assertEquals(20L, openAi.get("total_tokens"));
        assertEquals(new BigDecimal("0.3"), openAi.get("total_cost"));
        assertEquals(20.0, openAi.get("avg_latency_ms"));
    }

    @Test
    void normalizesOverviewAliasesAndIncludesConfiguredCatalogCounts() {
        when(mapper.getOverview(anyLong())).thenReturn(Map.of("TOTAL_CALLS", 3L, "AVG_LATENCY_MS", 12.5));
        when(mapper.selectLlmRecords(anyLong()))
                .thenReturn(
                        List.of(
                                record("LLM", "{\"totalTokens\":20,\"cost\":0.25}", 10),
                                record("LLM", "{\"totalTokens\":5,\"cost\":0.05}", 15)));
        Map<String, Object> overview = repository.getOverview();

        assertEquals(3L, overview.get("total_calls"));
        assertEquals(25L, overview.get("total_tokens"));
        assertEquals(new BigDecimal("0.30"), overview.get("total_cost"));
        assertEquals(12.5, overview.get("avg_latency_ms"));
        assertEquals(4L, overview.get("platform_count"));
        assertEquals(9L, overview.get("model_count"));
    }

    @Test
    void aggregatesPeriodsAndEmbeddingUsageInJava() {
        LocalDateTime now = LocalDateTime.now();
        when(mapper.selectLlmRecordsSince(any(), anyLong()))
                .thenReturn(
                        List.of(
                                record("LLM", "{\"totalTokens\":7,\"cost\":0.2}", 14, now),
                                record("LLM", "{\"totalTokens\":3,\"cost\":0.1}", 6, now)));
        when(mapper.selectEmbeddingRecords(anyLong()))
                .thenReturn(
                        List.of(
                                record(
                                        "EMBEDDING",
                                        "{\"estimatedTokens\":11,\"vectorCount\":2}",
                                        8,
                                        now),
                                record(
                                        "EMBEDDING",
                                        "{\"estimatedTokens\":9,\"vectorCount\":3}",
                                        12,
                                        now)));

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
        when(mapper.selectLlmTodayByTenantId(eq(1L), any()))
                .thenReturn(
                        List.of(
                                record("LLM", "{\"totalTokens\":4}", 1),
                                record("LLM", "{\"totalTokens\":6}", 1)));

        assertEquals(
                10L,
                TenantScope.withTenant(
                        new TenantId(1L),
                        () -> repository.sumLlmTodayTokensByTenantId(new TenantId(1L))));
        assertThrows(
                NullPointerException.class, () -> repository.sumLlmTodayTokensByTenantId(null));
    }

    @Test
    void rejectsUnattributedUsageBeforePersistence() {
        UsageRecordBO record = new UsageRecordBO();
        record.setUserId(7L);
        record.setSourceType("MODEL_CALL");
        record.setSourceId("run-1");

        assertThrows(IllegalArgumentException.class, () -> repository.insert(record));
    }

    @Test
    void insertsTenantScopedRecordAndMapsTheGeneratedRow() {
        UsageRecordBO record = new UsageRecordBO();
        record.setTenantId(9L);
        record.setUserId(7L);
        record.setSourceType("MODEL_CALL");
        record.setSourceId("run-1");
        record.setUsageType("LLM");
        record.setExtInfo("{}");

        try (MockedStatic<MapstructUtils> mapstruct = mockStatic(MapstructUtils.class)) {
            mapstruct
                    .when(
                            () ->
                                    MapstructUtils.convert(
                                            any(UsageRecordBO.class), eq(UsageRecordDO.class)))
                    .thenReturn(new UsageRecordDO());
            repository.insert(record);
            verify(mapper).insertSelective(any(UsageRecordDO.class));
        }
    }

    @Test
    void treatsDuplicateInsertAsAnIdempotentNoOp() {
        UsageRecordBO record = validRecord();
        try (MockedStatic<MapstructUtils> mapstruct = mockStatic(MapstructUtils.class)) {
            mapstruct
                    .when(
                            () ->
                                    MapstructUtils.convert(
                                            any(UsageRecordBO.class), eq(UsageRecordDO.class)))
                    .thenReturn(new UsageRecordDO());
            org.mockito.Mockito.doThrow(new DuplicateKeyException("duplicate"))
                    .when(mapper)
                    .insertSelective(any(UsageRecordDO.class));
            assertFalse(repository.insertIfAbsent(record));
        }
    }

    @Test
    void normalizesNullMapperRowsAndInvalidPeriodArguments() {
        when(mapper.aggregateByDay(any(Integer.class), anyLong())).thenReturn(null);
        when(mapper.aggregateByWeek(any(Integer.class), anyLong())).thenReturn(null);
        when(mapper.aggregateByMonth(any(Integer.class), anyLong())).thenReturn(null);

        assertTrue(repository.aggregateByDay(0).isEmpty());
        assertTrue(repository.aggregateByWeek(-1).isEmpty());
        assertTrue(repository.aggregateByMonth(2).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new TenantId(0L));
    }

    @Test
    void ignoresMalformedAndIncompleteRowsWhileKeepingStableFallbacks() {
        UsageRecordDO malformed = record("LLM", "not-json", 0);
        UsageRecordDO blank = record("LLM", "", 0);
        malformed.setCreateTime(null);
        blank.setCreateTime(null);
        when(mapper.selectLlmRecords(anyLong())).thenReturn(List.of(malformed, blank));
        when(mapper.getOverview(anyLong())).thenReturn(null);

        Map<String, Object> overview = repository.getOverview();
        assertEquals(0L, overview.get("total_calls"));
        assertEquals(0L, overview.get("total_tokens"));
        assertEquals(4L, overview.get("platform_count"));

        when(mapper.selectLlmRecordsSince(any(), anyLong())).thenReturn(List.of(malformed, blank));
        assertTrue(repository.aggregateLlmByMonth(1).isEmpty());
    }

    @Test
    void groupsPeriodsInReverseChronologicalOrderAndUsesUnknownModelLabels() {
        LocalDateTime older = LocalDateTime.of(2026, 1, 2, 3, 4);
        LocalDateTime newer = LocalDateTime.of(2026, 2, 3, 4, 5);
        when(mapper.selectLlmRecordsSince(any(), anyLong()))
                .thenReturn(
                        List.of(
                                record("LLM", "{\"totalTokens\":2,\"cost\":\"0.1\"}", 1, older),
                                record("LLM", "{\"totalTokens\":4,\"cost\":\"0.2\"}", 2, newer)));
        List<Map<String, Object>> rows = repository.aggregateLlmByMonth(12);
        assertEquals(2, rows.size());
        assertEquals("2026-02", rows.getFirst().get("usage_month"));
        assertEquals(4L, rows.getFirst().get("total_tokens"));

        when(mapper.selectLlmRecords(anyLong()))
                .thenReturn(List.of(record("LLM", "{\"totalTokens\":1,\"cost\":\"0.1\"}", 1)));
        Map<String, Object> fallback = repository.aggregateByModel().getFirst();
        assertEquals("UNKNOWN", fallback.get("platform"));
        assertEquals("UNKNOWN", fallback.get("model"));
    }

    @Test
    void normalizesDialectValuesAndNullCollectionsAcrossAllAggregationAdapters() {
        List<Map<String, Object>> rows = List.of(Map.of("usage_type", "LLM"));
        when(mapper.aggregateByDay(eq(1), anyLong())).thenReturn(rows);
        when(mapper.aggregateByWeek(eq(1), anyLong())).thenReturn(rows);
        when(mapper.aggregateByMonth(eq(1), anyLong())).thenReturn(rows);
        assertEquals(rows, repository.aggregateByDay(1));
        assertEquals(rows, repository.aggregateByWeek(1));
        assertEquals(rows, repository.aggregateByMonth(1));

        when(mapper.getOverview(anyLong())).thenReturn(Map.of("total_calls", "5"));
        when(mapper.selectLlmRecords(anyLong()))
                .thenReturn(
                        List.of(
                                record(
                                        "LLM",
                                        "{\"totalTokens\":\"bad\",\"cost\":\"bad\",\"platform\":\""
                                                + " \"}",
                                        0),
                                record("LLM", "{\"totalTokens\":2,\"cost\":0.1}", 0)));
        Map<String, Object> overview = repository.getOverview();
        assertEquals(5L, overview.get("total_calls"));
        assertEquals(2L, overview.get("total_tokens"));
        assertEquals(new BigDecimal("0.1"), overview.get("total_cost"));

        when(mapper.selectLlmTodayByTenantId(eq(9L), any())).thenReturn(null);
        assertEquals(0L, repository.sumLlmTodayTokensByTenantId(new TenantId(9L)));
        when(mapper.selectEmbeddingRecords(anyLong())).thenReturn(null);
        assertEquals(0L, repository.getEmbeddingOverview().get("total_calls"));
    }

    private static UsageRecordBO validRecord() {
        UsageRecordBO record = new UsageRecordBO();
        record.setTenantId(9L);
        record.setUserId(7L);
        record.setSourceType("MODEL_CALL");
        record.setSourceId("run-1");
        record.setUsageType("LLM");
        record.setExtInfo("{}");
        return record;
    }

    private static UsageRecordDO record(String usageType, String extInfo, long latencyMs) {
        return record(usageType, extInfo, latencyMs, LocalDateTime.now());
    }

    private static UsageRecordDO record(
            String usageType, String extInfo, long latencyMs, LocalDateTime createTime) {
        UsageRecordDO record = new UsageRecordDO();
        record.setUsageType(usageType);
        record.setExtInfo(extInfo);
        record.setLatencyMs(latencyMs);
        record.setCreateTime(createTime);
        return record;
    }
}
