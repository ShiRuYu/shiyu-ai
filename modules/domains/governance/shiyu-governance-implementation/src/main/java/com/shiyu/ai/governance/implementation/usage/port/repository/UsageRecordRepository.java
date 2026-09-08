package com.shiyu.ai.governance.implementation.usage.port.repository;

import com.shiyu.ai.governance.implementation.usage.domain.model.UsageRecordBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Map;

public interface UsageRecordRepository {
    void insert(UsageRecordBO record);

    /** Atomic idempotent write; implementations should rely on the database unique key. */
    default boolean insertIfAbsent(UsageRecordBO record) {
        insert(record);
        return true;
    }
    List<Map<String, Object>> aggregateByDay(int days);
    List<Map<String, Object>> aggregateByWeek(int weeks);
    List<Map<String, Object>> aggregateByMonth(int months);
    Map<String, Object> getOverview();
    List<Map<String, Object>> aggregateByModel();
    List<Map<String, Object>> aggregateLlmByDay(int days);
    List<Map<String, Object>> aggregateLlmByWeek(int weeks);
    List<Map<String, Object>> aggregateLlmByMonth(int months);
    Map<String, Object> getEmbeddingOverview();
    Long sumLlmTodayTokensByTenantId(TenantId tenantId);
}
