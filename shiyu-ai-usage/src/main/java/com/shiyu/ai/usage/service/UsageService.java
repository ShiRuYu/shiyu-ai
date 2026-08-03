package com.shiyu.ai.usage.service;

import java.util.List;
import java.util.Map;

/** Usage query/application boundary exposed to Web and other adapters. */
public interface UsageService {
    Map<String, Object> overview();
    List<Map<String, Object>> byDay(int days);
    List<Map<String, Object>> byWeek(int weeks);
    List<Map<String, Object>> byMonth(int months);
    List<Map<String, Object>> byModel();
    List<Map<String, Object>> llmByDay(int days);
    List<Map<String, Object>> llmByWeek(int weeks);
    List<Map<String, Object>> llmByMonth(int months);
    Map<String, Object> embeddingOverview();
}
