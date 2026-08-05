package com.shiyu.ai.dal.usage.repository;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.usage.dataobject.UsageRecordDO;
import com.shiyu.ai.dal.usage.mapper.UsageRecordMapper;
import com.shiyu.ai.usage.domain.model.UsageRecordBO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Unified usage-record data access and H2-compatible usage aggregation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UsageRecordRepositoryImpl implements com.shiyu.ai.usage.port.repository.UsageRecordRepository {

    private static final WeekFields ISO_WEEK = WeekFields.ISO;

    private final UsageRecordMapper usageRecordMapper;

    @Override
    public void insert(UsageRecordBO record) {
        UsageRecordDO data = MapstructUtils.convert(record, UsageRecordDO.class);
        usageRecordMapper.insertSelective(data);
        record.setId(data.getId());
    }

    @Override
    public List<Map<String, Object>> aggregateByDay(int days) {
        return safeRows(usageRecordMapper.aggregateByDay(days));
    }

    @Override
    public List<Map<String, Object>> aggregateByWeek(int weeks) {
        return safeRows(usageRecordMapper.aggregateByWeek(weeks));
    }

    @Override
    public List<Map<String, Object>> aggregateByMonth(int months) {
        return safeRows(usageRecordMapper.aggregateByMonth(months));
    }

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = usageRecordMapper.getOverview();
        return overview == null ? new LinkedHashMap<>() : overview;
    }

    @Override
    public List<Map<String, Object>> aggregateByModel() {
        Map<String, ModelMetrics> groups = new TreeMap<>();
        for (UsageRecordDO record : safeRecords(usageRecordMapper.selectLlmRecords())) {
            Map<String, Object> extInfo = parseExtInfo(record);
            String platform = textValue(extInfo.get("platform"), "UNKNOWN");
            String model = textValue(extInfo.get("model"), "UNKNOWN");
            ModelMetrics metrics = groups.computeIfAbsent(platform + '\u0000' + model,
                    ignored -> new ModelMetrics(platform, model));
            metrics.addLlm(record, extInfo);
        }

        return groups.values().stream()
                .sorted(Comparator.comparingLong(ModelMetrics::totalTokens).reversed()
                        .thenComparing(metrics -> metrics.platform)
                        .thenComparing(metrics -> metrics.model))
                .map(metrics -> {
                    Map<String, Object> row = metrics.toLlmRow();
                    row.put("platform", metrics.platform);
                    row.put("model", metrics.model);
                    return row;
                })
                .toList();
    }

    @Override
    public List<Map<String, Object>> aggregateLlmByDay(int days) {
        return aggregateLlmByPeriod(daysBefore(days), "usage_date",
                time -> time.toLocalDate().toString());
    }

    @Override
    public List<Map<String, Object>> aggregateLlmByWeek(int weeks) {
        return aggregateLlmByPeriod(weeksBefore(weeks), "usage_week", this::weekKey);
    }

    @Override
    public List<Map<String, Object>> aggregateLlmByMonth(int months) {
        return aggregateLlmByPeriod(monthsBefore(months), "usage_month",
                time -> YearMonth.from(time).toString());
    }

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

    @Override
    public Long sumLlmTodayTokensByTenantId(Long tenantId) {
        if (tenantId == null) {
            return 0L;
        }
        return safeRecords(usageRecordMapper.selectLlmTodayByTenantId(tenantId, LocalDate.now().atStartOfDay()))
                .stream()
                .map(this::parseExtInfo)
                .mapToLong(extInfo -> longValue(extInfo.get("totalTokens")))
                .sum();
    }

    private List<Map<String, Object>> aggregateLlmByPeriod(LocalDateTime start, String keyName,
                                                             Function<LocalDateTime, String> keyFunction) {
        Map<String, UsageMetrics> groups = new TreeMap<>(Comparator.reverseOrder());
        for (UsageRecordDO record : safeRecords(usageRecordMapper.selectLlmRecordsSince(start))) {
            if (record.getCreateTime() == null) {
                continue;
            }
            groups.computeIfAbsent(keyFunction.apply(record.getCreateTime()), ignored -> new UsageMetrics())
                    .addLlm(record, parseExtInfo(record));
        }
        List<Map<String, Object>> result = new ArrayList<>(groups.size());
        groups.forEach((key, metrics) -> {
            Map<String, Object> row = metrics.toLlmRow();
            row.put(keyName, key);
            result.add(row);
        });
        return result;
    }

    private LocalDateTime daysBefore(int days) {
        return LocalDateTime.now().minusDays(Math.max(0, days));
    }

    private LocalDateTime weeksBefore(int weeks) {
        return LocalDateTime.now().minusWeeks(Math.max(0, weeks));
    }

    private LocalDateTime monthsBefore(int months) {
        return LocalDateTime.now().minusMonths(Math.max(0, months));
    }

    private String weekKey(LocalDateTime time) {
        LocalDate date = time.toLocalDate();
        return String.format(Locale.ROOT, "%04d-%02d",
                date.get(ISO_WEEK.weekBasedYear()), date.get(ISO_WEEK.weekOfWeekBasedYear()));
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

    private static class UsageMetrics {
        private long calls;
        private long latencySamples;
        private long totalLatency;
        private long totalTokens;
        private long totalEstimatedTokens;
        private long totalVectors;
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

    private static final class ModelMetrics extends UsageMetrics {
        private final String platform;
        private final String model;

        private ModelMetrics(String platform, String model) {
            this.platform = platform;
            this.model = model;
        }
    }
}
