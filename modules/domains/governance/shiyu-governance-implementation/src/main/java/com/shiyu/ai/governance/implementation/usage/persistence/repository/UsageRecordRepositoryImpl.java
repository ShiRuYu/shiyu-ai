package com.shiyu.ai.governance.implementation.usage.persistence.repository;

import com.shiyu.ai.common.core.jdbc.JdbcDialect;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.governance.implementation.usage.domain.model.UsageRecordBO;
import com.shiyu.ai.governance.implementation.usage.persistence.dataobject.UsageRecordDO;
import com.shiyu.ai.governance.implementation.usage.persistence.mapper.UsageRecordMapper;
import com.shiyu.ai.governance.implementation.usage.port.repository.UsageRecordRepository;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.model.contract.api.ModelCatalogPort;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;

import javax.sql.DataSource;

/**
 * 实现治理用量记录的租户隔离查询和持久化。
 */
@Slf4j
@Component
public class UsageRecordRepositoryImpl implements UsageRecordRepository {

    /**
     * ISO_WEEK 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final WeekFields ISO_WEEK = WeekFields.ISO;

    /**
     * usageRecordMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final UsageRecordMapper usageRecordMapper;
    /**
     * modelCatalog 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ModelCatalogPort modelCatalog;
    /**
     * dialect 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final JdbcDialect dialect;

    /**
     * 处理用量记录repositoryimpl。
     *
     * @param usageRecordMapper usageRecordMapper 参数。
     * @param modelCatalog modelCatalog 参数。
     *
     * @return 处理结果。
     */
    public UsageRecordRepositoryImpl(
            UsageRecordMapper usageRecordMapper, ModelCatalogPort modelCatalog) {
        this(usageRecordMapper, modelCatalog, JdbcDialect.fromProduct("H2"));
    }

    /**
     * {@code UsageRecordRepositoryImpl} 创建并初始化当前类型实例。
     *
     * @param usageRecordMapper 参数值，用于执行当前操作。
     * @param modelCatalog 参数值，用于执行当前操作。
     * @param dataSource 参数值，用于执行当前操作。
     */
    @Autowired
    public UsageRecordRepositoryImpl(
            UsageRecordMapper usageRecordMapper,
            ModelCatalogPort modelCatalog,
            @Qualifier("agentDataSource") DataSource dataSource) {
        this(usageRecordMapper, modelCatalog, JdbcDialect.detect(new JdbcTemplate(dataSource)));
    }

    private UsageRecordRepositoryImpl(
            UsageRecordMapper usageRecordMapper,
            ModelCatalogPort modelCatalog,
            JdbcDialect dialect) {
        this.usageRecordMapper = usageRecordMapper;
        this.modelCatalog = modelCatalog;
        this.dialect = dialect;
    }

    /**
     * {@code insert} 执行当前类型定义的业务操作。
     *
     * @param record 参数值，用于执行当前操作。
     */
    @Override
    public void insert(UsageRecordBO record) {
        validateTenantScopedRecord(record);
        UsageRecordDO data = MapstructUtils.convert(record, UsageRecordDO.class);
        usageRecordMapper.insertSelective(data);
        record.setId(data.getId());
    }

    /**
     * {@code insertIfAbsent} 执行当前类型定义的业务操作。
     *
     * @param record 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public boolean insertIfAbsent(UsageRecordBO record) {
        try {
            insert(record);
            return true;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }

    /**
     * {@code aggregateByDay} 执行当前类型定义的业务操作。
     *
     * @param days 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Map<String, Object>> aggregateByDay(int days) {
        if (days <= 0) return List.of();
        List<UsageRecordDO> portable = usageRecordMapper.selectRecordsSince(daysBefore(days));
        if (usesPortableAggregation() && portable != null) {
            return aggregateRecords(portable, "usage_date", time -> time.toLocalDate().toString());
        }
        return safeRows(usageRecordMapper.aggregateByDay(days));
    }

    /**
     * {@code aggregateByWeek} 执行当前类型定义的业务操作。
     *
     * @param weeks 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Map<String, Object>> aggregateByWeek(int weeks) {
        if (weeks <= 0) return List.of();
        List<UsageRecordDO> portable = usageRecordMapper.selectRecordsSince(weeksBefore(weeks));
        if (usesPortableAggregation() && portable != null) {
            return aggregateRecords(portable, "usage_week", this::weekKey);
        }
        return safeRows(usageRecordMapper.aggregateByWeek(weeks));
    }

    /**
     * {@code aggregateByMonth} 执行当前类型定义的业务操作。
     *
     * @param months 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Map<String, Object>> aggregateByMonth(int months) {
        if (months <= 0) return List.of();
        List<UsageRecordDO> portable = usageRecordMapper.selectRecordsSince(monthsBefore(months));
        if (usesPortableAggregation() && portable != null) {
            return aggregateRecords(
                    portable, "usage_month", time -> YearMonth.from(time).toString());
        }
        return safeRows(usageRecordMapper.aggregateByMonth(months));
    }

    /**
     * {@code getOverview} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> databaseOverview = usageRecordMapper.getOverview();
        Map<String, Object> raw = databaseOverview == null ? Map.of() : databaseOverview;
        UsageMetrics llmMetrics = new UsageMetrics();
        for (UsageRecordDO record : safeRecords(usageRecordMapper.selectLlmRecords())) {
            llmMetrics.addLlm(record, parseExtInfo(record));
        }
        Map<String, Object> llmOverview = llmMetrics.toLlmRow();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total_calls", longValue(valueIgnoreCase(raw, "total_calls")));
        result.put("total_tokens", llmOverview.get("total_tokens"));
        result.put("total_cost", llmOverview.get("total_cost"));
        result.put("avg_latency_ms", valueIgnoreCase(raw, "avg_latency_ms"));
        result.put("platform_count", modelCatalog.countEnabledPlatforms());
        result.put("model_count", modelCatalog.countEnabledModels());
        return result;
    }

    /**
     * {@code aggregateByModel} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Map<String, Object>> aggregateByModel() {
        Map<String, ModelMetrics> groups = new TreeMap<>();
        for (UsageRecordDO record : safeRecords(usageRecordMapper.selectLlmRecords())) {
            Map<String, Object> extInfo = parseExtInfo(record);
            String platform = textValue(extInfo.get("platform"), "UNKNOWN");
            String model = textValue(extInfo.get("model"), "UNKNOWN");
            ModelMetrics metrics =
                    groups.computeIfAbsent(
                            platform + '\u0000' + model,
                            ignored -> new ModelMetrics(platform, model));
            metrics.addLlm(record, extInfo);
        }

        return groups.values().stream()
                .sorted(
                        Comparator.comparingLong(ModelMetrics::totalTokens)
                                .reversed()
                                .thenComparing(metrics -> metrics.platform)
                                .thenComparing(metrics -> metrics.model))
                .map(
                        metrics -> {
                            Map<String, Object> row = metrics.toLlmRow();
                            row.put("platform", metrics.platform);
                            row.put("model", metrics.model);
                            return row;
                        })
                .toList();
    }

    /**
     * {@code aggregateLlmByDay} 执行当前类型定义的业务操作。
     *
     * @param days 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Map<String, Object>> aggregateLlmByDay(int days) {
        return aggregateLlmByPeriod(
                daysBefore(days), "usage_date", time -> time.toLocalDate().toString());
    }

    /**
     * {@code aggregateLlmByWeek} 执行当前类型定义的业务操作。
     *
     * @param weeks 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Map<String, Object>> aggregateLlmByWeek(int weeks) {
        return aggregateLlmByPeriod(weeksBefore(weeks), "usage_week", this::weekKey);
    }

    /**
     * {@code aggregateLlmByMonth} 执行当前类型定义的业务操作。
     *
     * @param months 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Map<String, Object>> aggregateLlmByMonth(int months) {
        return aggregateLlmByPeriod(
                monthsBefore(months), "usage_month", time -> YearMonth.from(time).toString());
    }

    /**
     * {@code getEmbeddingOverview} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Map<String, Object> getEmbeddingOverview() {
        UsageMetrics metrics = new UsageMetrics();
        for (UsageRecordDO record : safeRecords(usageRecordMapper.selectEmbeddingRecords())) {
            metrics.addEmbedding(record, parseExtInfo(record));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total_calls", metrics.calls);
        result.put("total_estimated_tokens", metrics.totalEstimatedTokens);
        result.put("total_vectors", metrics.totalVectors);
        result.put("avg_latency_ms", metrics.averageLatency());
        return result;
    }

    /**
     * {@code sumLlmTodayTokensByTenantId} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Long sumLlmTodayTokensByTenantId(TenantId tenantId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        return safeRecords(
                        usageRecordMapper.selectLlmTodayByTenantId(
                                tenantId.value(), LocalDate.now().atStartOfDay()))
                .stream()
                .map(this::parseExtInfo)
                .mapToLong(extInfo -> longValue(extInfo.get("totalTokens")))
                .sum();
    }

    /**
     * 校验租户scoped记录。
     *
     * @param record record 参数。
     */
    private static void validateTenantScopedRecord(UsageRecordBO record) {
        if (record == null) {
            throw new IllegalArgumentException("usage record must not be null");
        }
        if (record.getTenantId() == null || record.getTenantId() <= 0) {
            throw new IllegalArgumentException("usage record tenantId must be positive");
        }
        if (record.getUserId() == null || record.getUserId() <= 0) {
            throw new IllegalArgumentException("usage record userId must be positive");
        }
        if (record.getSourceType() == null
                || record.getSourceType().isBlank()
                || record.getSourceId() == null
                || record.getSourceId().isBlank()) {
            throw new IllegalArgumentException("usage record sourceType and sourceId are required");
        }
    }

    private List<Map<String, Object>> aggregateLlmByPeriod(
            LocalDateTime start, String keyName, Function<LocalDateTime, String> keyFunction) {
        Map<String, UsageMetrics> groups = new TreeMap<>(Comparator.reverseOrder());
        for (UsageRecordDO record : safeRecords(usageRecordMapper.selectLlmRecordsSince(start))) {
            if (record.getCreateTime() == null) {
                continue;
            }
            groups.computeIfAbsent(
                            keyFunction.apply(record.getCreateTime()),
                            ignored -> new UsageMetrics())
                    .addLlm(record, parseExtInfo(record));
        }
        List<Map<String, Object>> result = new ArrayList<>(groups.size());
        groups.forEach(
                (key, metrics) -> {
                    Map<String, Object> row = metrics.toLlmRow();
                    row.put(keyName, key);
                    result.add(row);
                });
        return result;
    }

    private List<Map<String, Object>> aggregateRecords(
            List<UsageRecordDO> records,
            String keyName,
            Function<LocalDateTime, String> keyFunction) {
        Map<String, AggregateRow> groups = new TreeMap<>(Comparator.reverseOrder());
        for (UsageRecordDO record : records) {
            if (record == null || record.getCreateTime() == null) continue;
            String key = keyFunction.apply(record.getCreateTime());
            String usageType = record.getUsageType();
            String groupKey = key + '\u0000' + (usageType == null ? "" : usageType);
            AggregateRow row =
                    groups.computeIfAbsent(groupKey, ignored -> new AggregateRow(key, usageType));
            row.calls++;
            if (record.getLatencyMs() != null) {
                row.latencyTotal += record.getLatencyMs();
                row.latencySamples++;
            }
        }
        List<Map<String, Object>> result = new ArrayList<>(groups.size());
        groups.forEach(
                (ignored, row) -> {
                    Map<String, Object> values = new LinkedHashMap<>();
                    values.put(keyName, row.periodKey);
                    values.put("usage_type", row.usageType);
                    values.put("call_count", row.calls);
                    values.put(
                            "avg_latency_ms",
                            row.latencySamples == 0
                                    ? null
                                    : row.latencyTotal / (double) row.latencySamples);
                    result.add(values);
                });
        return result;
    }

    /**
     * {@code AggregateRow} 承载治理模块的领域状态或协作行为，负责维护本类型的职责边界。
     */
    private static final class AggregateRow {
        private final String periodKey;
        /**
         * 用量类型，表示当前对象中的对应属性。
         */
        private final String usageType;
        /**
         * calls 属性，保存当前对象中的业务数据或协作依赖。
         */
        private long calls;
        /**
         * latencyTotal 属性，保存当前对象中的业务数据或协作依赖。
         */
        private long latencyTotal;
        /**
         * latencySamples 属性，保存当前对象中的业务数据或协作依赖。
         */
        private long latencySamples;

        private AggregateRow(String periodKey, String usageType) {
            this.periodKey = periodKey;
            this.usageType = usageType;
        }
    }

    private LocalDateTime daysBefore(int days) {
        return LocalDateTime.now().minusDays(Math.max(0, days));
    }

    private boolean usesPortableAggregation() {
        return dialect.kind() == JdbcDialect.Kind.POSTGRESQL
                || dialect.kind() == JdbcDialect.Kind.MYSQL;
    }

    private LocalDateTime weeksBefore(int weeks) {
        return LocalDateTime.now().minusWeeks(Math.max(0, weeks));
    }

    private LocalDateTime monthsBefore(int months) {
        return LocalDateTime.now().minusMonths(Math.max(0, months));
    }

    private String weekKey(LocalDateTime time) {
        LocalDate date = time.toLocalDate();
        return String.format(
                Locale.ROOT,
                "%04d-%02d",
                date.get(ISO_WEEK.weekBasedYear()),
                date.get(ISO_WEEK.weekOfWeekBasedYear()));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseExtInfo(UsageRecordDO record) {
        String extInfo = record.getExtInfo();
        if (extInfo == null || extInfo.isBlank()) {
            return Map.of();
        }
        try {
            Map<String, Object> values = JSONUtils.getObjectMapper().readValue(extInfo, Map.class);
            return values == null ? Map.of() : values;
        } catch (Exception exception) {
            log.warn("Ignoring malformed usage extInfo for record {}", record.getId(), exception);
            return Map.of();
        }
    }

    private static List<Map<String, Object>> safeRows(List<Map<String, Object>> rows) {
        return rows == null ? List.of() : rows;
    }

    private static List<UsageRecordDO> safeRecords(List<UsageRecordDO> records) {
        return records == null ? List.of() : records;
    }

    private static String textValue(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String text = String.valueOf(value);
        return text.isBlank() ? fallback : text;
    }

    private static long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private static Object valueIgnoreCase(Map<String, Object> values, String key) {
        if (values.containsKey(key)) {
            return values.get(key);
        }
        return values.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(key))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private static BigDecimal decimalValue(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * {@code UsageMetrics} 承载治理模块的领域状态或协作行为，负责维护本类型的职责边界。
     */
    private static class UsageMetrics {
        private long calls;
        /**
         * latencySamples 属性，保存当前对象中的业务数据或协作依赖。
         */
        private long latencySamples;
        /**
         * totalLatency 属性，保存当前对象中的业务数据或协作依赖。
         */
        private long totalLatency;
        /**
         * totalTokens 属性，保存当前对象中的业务数据或协作依赖。
         */
        private long totalTokens;
        /**
         * totalEstimatedTokens 属性，保存当前对象中的业务数据或协作依赖。
         */
        private long totalEstimatedTokens;
        /**
         * totalVectors 属性，保存当前对象中的业务数据或协作依赖。
         */
        private long totalVectors;
        /**
         * totalCost 属性，保存当前对象中的业务数据或协作依赖。
         */
        private BigDecimal totalCost = BigDecimal.ZERO;

        void addLlm(UsageRecordDO record, Map<String, Object> extInfo) {
            addLatency(record);
            totalTokens += longValue(extInfo.get("totalTokens"));
            totalCost = totalCost.add(decimalValue(extInfo.get("cost")));
        }

        void addEmbedding(UsageRecordDO record, Map<String, Object> extInfo) {
            addLatency(record);
            totalEstimatedTokens += longValue(extInfo.get("estimatedTokens"));
            totalVectors += longValue(extInfo.get("vectorCount"));
        }

        private void addLatency(UsageRecordDO record) {
            calls++;
            if (record.getLatencyMs() != null) {
                totalLatency += record.getLatencyMs();
                latencySamples++;
            }
        }

        Double averageLatency() {
            return latencySamples == 0 ? null : totalLatency / (double) latencySamples;
        }

        long totalTokens() {
            return totalTokens;
        }

        Map<String, Object> toLlmRow() {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("call_count", calls);
            row.put("total_tokens", totalTokens);
            row.put("total_cost", totalCost);
            row.put("avg_latency_ms", averageLatency());
            return row;
        }
    }

    /**
     * {@code ModelMetrics} 承载治理模块的领域状态或协作行为，负责维护本类型的职责边界。
     */
    private static final class ModelMetrics extends UsageMetrics {
        private final String platform;
        /**
         * model 属性，保存当前对象中的业务数据或协作依赖。
         */
        private final String model;

        private ModelMetrics(String platform, String model) {
            this.platform = platform;
            this.model = model;
        }
    }
}
