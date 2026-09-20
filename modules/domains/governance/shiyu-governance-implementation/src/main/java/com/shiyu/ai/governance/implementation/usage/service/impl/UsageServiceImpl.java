package com.shiyu.ai.governance.implementation.usage.service.impl;

import com.shiyu.ai.governance.implementation.usage.port.repository.UsageRecordRepository;
import com.shiyu.ai.governance.implementation.usage.port.UsageService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 提供 用量 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Service
@RequiredArgsConstructor
public class UsageServiceImpl implements UsageService {
    /**
     * 仓储，表示当前对象中的对应属性。
     */
    private final UsageRecordRepository repository;

    /**
     * 执行 用量 相关业务数据，并返回处理结果。
     *
     * @return 返回 用量 相关操作生成的结果数据。
     */
    public Map<String, Object> overview() {
        return repository.getOverview();
    }

    /**
     * 查询 用量 相关业务数据，并返回处理结果。
     *
     * @param days 用于完成本次业务处理的 days 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<Map<String, Object>> byDay(int days) {
        return repository.aggregateByDay(days);
    }

    /**
     * 查询 用量 相关业务数据，并返回处理结果。
     *
     * @param weeks 用于完成本次业务处理的 weeks 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<Map<String, Object>> byWeek(int weeks) {
        return repository.aggregateByWeek(weeks);
    }

    /**
     * 查询 用量 相关业务数据，并返回处理结果。
     *
     * @param months 用于完成本次业务处理的 months 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<Map<String, Object>> byMonth(int months) {
        return repository.aggregateByMonth(months);
    }

    /**
     * 查询 用量 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<Map<String, Object>> byModel() {
        return repository.aggregateByModel();
    }

    /**
     * 执行 用量 相关业务数据，并返回处理结果。
     *
     * @param days 用于完成本次业务处理的 days 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<Map<String, Object>> llmByDay(int days) {
        return repository.aggregateLlmByDay(days);
    }

    /**
     * 执行 用量 相关业务数据，并返回处理结果。
     *
     * @param weeks 用于完成本次业务处理的 weeks 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<Map<String, Object>> llmByWeek(int weeks) {
        return repository.aggregateLlmByWeek(weeks);
    }

    /**
     * 执行 用量 相关业务数据，并返回处理结果。
     *
     * @param months 用于完成本次业务处理的 months 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<Map<String, Object>> llmByMonth(int months) {
        return repository.aggregateLlmByMonth(months);
    }

    /**
     * 执行 用量 相关业务数据，并返回处理结果。
     *
     * @return 返回 用量 相关操作生成的结果数据。
     */
    public Map<String, Object> embeddingOverview() {
        return repository.getEmbeddingOverview();
    }
}
