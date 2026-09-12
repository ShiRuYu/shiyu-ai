package com.shiyu.ai.governance.implementation.usage.service.impl;

import com.shiyu.ai.governance.implementation.usage.port.repository.UsageRecordRepository;
import com.shiyu.ai.governance.implementation.usage.service.UsageService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * {@code UsageServiceImpl} 实现治理模块的应用服务，负责编排用例流程并维护业务边界。
 */
@Service
@RequiredArgsConstructor
public class UsageServiceImpl implements UsageService {
    /**
     * 仓储，表示当前对象中的对应属性。
     */
    private final UsageRecordRepository repository;

    /**
     * {@code overview} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public Map<String, Object> overview() {
        return repository.getOverview();
    }

    /**
     * {@code byDay} 执行当前类型定义的业务操作。
     *
     * @param days 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<Map<String, Object>> byDay(int days) {
        return repository.aggregateByDay(days);
    }

    /**
     * {@code byWeek} 执行当前类型定义的业务操作。
     *
     * @param weeks 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<Map<String, Object>> byWeek(int weeks) {
        return repository.aggregateByWeek(weeks);
    }

    /**
     * {@code byMonth} 执行当前类型定义的业务操作。
     *
     * @param months 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<Map<String, Object>> byMonth(int months) {
        return repository.aggregateByMonth(months);
    }

    /**
     * {@code byModel} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<Map<String, Object>> byModel() {
        return repository.aggregateByModel();
    }

    /**
     * {@code llmByDay} 执行当前类型定义的业务操作。
     *
     * @param days 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<Map<String, Object>> llmByDay(int days) {
        return repository.aggregateLlmByDay(days);
    }

    /**
     * {@code llmByWeek} 执行当前类型定义的业务操作。
     *
     * @param weeks 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<Map<String, Object>> llmByWeek(int weeks) {
        return repository.aggregateLlmByWeek(weeks);
    }

    /**
     * {@code llmByMonth} 执行当前类型定义的业务操作。
     *
     * @param months 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<Map<String, Object>> llmByMonth(int months) {
        return repository.aggregateLlmByMonth(months);
    }

    /**
     * {@code embeddingOverview} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public Map<String, Object> embeddingOverview() {
        return repository.getEmbeddingOverview();
    }
}
