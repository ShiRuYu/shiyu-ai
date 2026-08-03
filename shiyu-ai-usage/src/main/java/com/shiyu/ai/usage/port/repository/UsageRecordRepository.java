package com.shiyu.ai.usage.port.repository;

import com.shiyu.ai.usage.domain.model.UsageRecordBO;

import java.util.List;
import java.util.Map;

public interface UsageRecordRepository {
    void insert(UsageRecordBO record);
    List<Map<String, Object>> aggregateByDay(int days);
    List<Map<String, Object>> aggregateByWeek(int weeks);
    List<Map<String, Object>> aggregateByMonth(int months);
    Map<String, Object> getOverview();
    List<Map<String, Object>> aggregateByModel();
    List<Map<String, Object>> aggregateLlmByDay(int days);
    List<Map<String, Object>> aggregateLlmByWeek(int weeks);
    List<Map<String, Object>> aggregateLlmByMonth(int months);
    Map<String, Object> getEmbeddingOverview();
    Long sumLlmTodayTokensByTenantId(Long tenantId);
}
