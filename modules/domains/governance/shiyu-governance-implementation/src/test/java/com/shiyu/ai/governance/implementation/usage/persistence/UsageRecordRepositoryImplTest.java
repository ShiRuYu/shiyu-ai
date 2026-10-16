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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.common.foundation.jdbc.JdbcDialect;
import com.shiyu.ai.governance.implementation.usage.domain.model.UsageRecordBO;
import com.shiyu.ai.governance.implementation.usage.persistence.dataobject.UsageRecordDO;
import com.shiyu.ai.governance.implementation.usage.persistence.mapper.UsageRecordMapper;
import com.shiyu.ai.governance.implementation.usage.persistence.repository.UsageRecordRepositoryImpl;
import com.shiyu.ai.governance.implementation.usage.persistence.repository.PlatformUsageRepositoryImpl;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.model.contract.api.ModelCatalogPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.h2.jdbcx.JdbcDataSource;
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

    @Test
    void platformOverviewUsesAllTenantsWithoutTenantScope() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:platform-usage-test;DB_CLOSE_DELAY=-1");
        PlatformUsageRepositoryImpl platform =
                new PlatformUsageRepositoryImpl(mapper, modelCatalog, dataSource);
        when(mapper.getOverviewAllTenants()).thenReturn(Map.of("total_calls", 3L));
        when(mapper.selectAllLlmRecords()).thenReturn(List.of());

        TenantScope.clear();
        assertEquals(3L, platform.getOverview().get("total_calls"));
        verify(mapper).getOverviewAllTenants();
        verify(mapper).selectAllLlmRecords();
    }

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
    void aggregatesPersistedTokenColumnsWhenExtInfoHasNoTotalTokens() {
        UsageRecordDO record =
                record("LLM", "{\"platform\":\"deepseek\",\"model\":\"deepseek-flash\"}", 10);
        record.setInputTokens(49L);
        record.setOutputTokens(45L);
        when(mapper.selectLlmRecords(anyLong())).thenReturn(List.of(record));

        assertEquals(94L, repository.aggregateByModel().getFirst().get("total_tokens"));
        assertEquals(94L, repository.getOverview().get("total_tokens"));
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
    void rejectsInvalidUserSourceAndCrossTenantUsageBeforePersistence() {
        UsageRecordBO missingUser = validRecord();
        missingUser.setUserId(null);
        UsageRecordBO missingSourceType = validRecord();
        missingSourceType.setSourceType(" ");
        UsageRecordBO missingSourceId = validRecord();
        missingSourceId.setSourceId(null);
        UsageRecordBO anotherTenant = validRecord();
        anotherTenant.setTenantId(10L);

        assertThrows(IllegalArgumentException.class, () -> repository.insert(missingUser));
        assertThrows(IllegalArgumentException.class, () -> repository.insert(missingSourceType));
        assertThrows(IllegalArgumentException.class, () -> repository.insert(missingSourceId));
        assertThrows(RuntimeException.class, () -> repository.insert(anotherTenant));
        verify(mapper, never()).insertSelective(any(UsageRecordDO.class));
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
        verify(mapper, never()).selectRecordsSince(any(), anyLong());

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

    @Test
    void portableAggregationGroupsUsageTypesAndAveragesOnlyKnownLatency() {
        UsageRecordRepositoryImpl portable = new PortableRepository(mapper, modelCatalog, false);
        LocalDateTime today = LocalDateTime.now();
        UsageRecordDO noLatency = record("LLM", "{}", 0, today);
        noLatency.setLatencyMs(null);
        when(mapper.selectRecordsSince(any(), eq(9L))).thenReturn(List.of(
                record("LLM", "{}", 20, today), noLatency,
                record("EMBEDDING", "{}", 8, today)));

        List<Map<String, Object>> byDay = portable.aggregateByDay(1);
        Map<String, Object> llm = byDay.stream()
                .filter(row -> "LLM".equals(row.get("usage_type"))).findFirst().orElseThrow();
        Map<String, Object> embedding = byDay.stream()
                .filter(row -> "EMBEDDING".equals(row.get("usage_type")))
                .findFirst().orElseThrow();
        assertEquals(2L, llm.get("call_count"));
        assertEquals(20.0, llm.get("avg_latency_ms"));
        assertEquals(1L, embedding.get("call_count"));
        assertEquals(8.0, embedding.get("avg_latency_ms"));
        assertEquals(2, portable.aggregateByWeek(1).size());
        assertEquals(2, portable.aggregateByMonth(1).size());
        verify(mapper, never()).aggregateByDay(any(Integer.class), anyLong());
    }

    @Test
    void platformPortableAggregationIncludesOtherTenantsWithoutARequestScope() {
        UsageRecordRepositoryImpl platform = new PortableRepository(mapper, modelCatalog, true);
        LocalDateTime today = LocalDateTime.now();
        UsageRecordDO first = record("LLM", "{}", 10, today);
        first.setTenantId(9L);
        UsageRecordDO second = record("LLM", "{}", 30, today);
        second.setTenantId(10L);
        when(mapper.selectAllRecordsSince(any())).thenReturn(List.of(first, second));

        TenantScope.clear();
        Map<String, Object> allTenants = platform.aggregateByDay(1).getFirst();

        assertEquals(2L, allTenants.get("call_count"));
        assertEquals(20.0, allTenants.get("avg_latency_ms"));
        verify(mapper, never()).selectRecordsSince(any(), anyLong());
    }

    @Test
    void platformH2ReportsUseAllTenantQueriesWithoutARequestScope() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:platform-period-usage;DB_CLOSE_DELAY=-1");
        PlatformUsageRepositoryImpl platform =
                new PlatformUsageRepositoryImpl(mapper, modelCatalog, dataSource);
        LocalDateTime today = LocalDateTime.now();
        List<Map<String, Object>> periodRows = List.of(Map.of("usage_type", "LLM", "call_count", 2L));
        UsageRecordDO first = record("LLM",
                "{\"platform\":\"p\",\"model\":\"m\",\"totalTokens\":4,\"cost\":0.1}",
                10, today);
        first.setTenantId(9L);
        UsageRecordDO second = record("LLM",
                "{\"platform\":\"p\",\"model\":\"m\",\"totalTokens\":6,\"cost\":0.2}",
                30, today);
        second.setTenantId(10L);
        when(mapper.aggregateByDayAllTenants(1)).thenReturn(periodRows);
        when(mapper.aggregateByWeekAllTenants(1)).thenReturn(periodRows);
        when(mapper.aggregateByMonthAllTenants(1)).thenReturn(periodRows);
        when(mapper.selectAllLlmRecords()).thenReturn(List.of(first, second));
        when(mapper.selectAllLlmRecordsSince(any())).thenReturn(List.of(first, second));
        when(mapper.selectAllEmbeddingRecords()).thenReturn(List.of(
                record("EMBEDDING", "{\"estimatedTokens\":3,\"vectorCount\":2}", 12, today)));

        TenantScope.clear();
        assertEquals(periodRows, platform.aggregateByDay(1));
        assertEquals(periodRows, platform.aggregateByWeek(1));
        assertEquals(periodRows, platform.aggregateByMonth(1));
        assertEquals(10L, platform.aggregateByModel().getFirst().get("total_tokens"));
        assertEquals(10L, platform.aggregateLlmByDay(1).getFirst().get("total_tokens"));
        assertEquals(10L, platform.aggregateLlmByWeek(1).getFirst().get("total_tokens"));
        assertEquals(10L, platform.aggregateLlmByMonth(1).getFirst().get("total_tokens"));
        assertEquals(3L, platform.getEmbeddingOverview().get("total_estimated_tokens"));
        verify(mapper, never()).selectRecordsSince(any(), anyLong());
        verify(mapper, never()).selectLlmRecords(anyLong());
    }

    @Test
    void portableAggregationFallsBackToSqlWhenRawRecordsAreUnavailable() {
        UsageRecordRepositoryImpl portable = new PortableRepository(mapper, modelCatalog, false);
        List<Map<String, Object>> fallback = List.of(Map.of("usage_type", "LLM", "call_count", 3L));
        when(mapper.selectRecordsSince(any(), eq(9L))).thenReturn(null);
        when(mapper.aggregateByDay(eq(1), eq(9L))).thenReturn(fallback);
        when(mapper.aggregateByWeek(eq(1), eq(9L))).thenReturn(fallback);
        when(mapper.aggregateByMonth(eq(1), eq(9L))).thenReturn(fallback);

        assertEquals(fallback, portable.aggregateByDay(1));
        assertEquals(fallback, portable.aggregateByWeek(1));
        assertEquals(fallback, portable.aggregateByMonth(1));
        verify(mapper).aggregateByDay(1, 9L);
        verify(mapper).aggregateByWeek(1, 9L);
        verify(mapper).aggregateByMonth(1, 9L);
    }

    /** 无需连接实际 PostgreSQL，即可验证可移植 SQL 聚合路径。 */
    private static final class PortableRepository extends UsageRecordRepositoryImpl {
        private PortableRepository(UsageRecordMapper mapper, ModelCatalogPort catalog,
                boolean allTenants) {
            super(mapper, catalog, JdbcDialect.fromProduct("PostgreSQL"), allTenants);
        }
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
