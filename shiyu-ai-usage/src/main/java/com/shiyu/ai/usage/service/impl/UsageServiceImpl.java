package com.shiyu.ai.usage.service.impl;

import com.shiyu.ai.dal.agent.repository.UsageRecordRepository;
import com.shiyu.ai.usage.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsageServiceImpl implements UsageService {
    private final UsageRecordRepository repository;

    public Map<String, Object> overview() { return repository.getOverview(); }
    public List<Map<String, Object>> byDay(int days) { return repository.aggregateByDay(days); }
    public List<Map<String, Object>> byWeek(int weeks) { return repository.aggregateByWeek(weeks); }
    public List<Map<String, Object>> byMonth(int months) { return repository.aggregateByMonth(months); }
    public List<Map<String, Object>> byModel() { return repository.aggregateByModel(); }
    public List<Map<String, Object>> llmByDay(int days) { return repository.aggregateLlmByDay(days); }
    public List<Map<String, Object>> llmByWeek(int weeks) { return repository.aggregateLlmByWeek(weeks); }
    public List<Map<String, Object>> llmByMonth(int months) { return repository.aggregateLlmByMonth(months); }
    public Map<String, Object> embeddingOverview() { return repository.getEmbeddingOverview(); }
}
