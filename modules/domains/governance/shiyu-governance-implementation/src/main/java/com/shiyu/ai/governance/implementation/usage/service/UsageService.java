package com.shiyu.ai.governance.implementation.usage.service;

import java.util.List;
import java.util.Map;

/**
 * UsageService 服务接口，负责执行治理领域相关业务操作。
 */
public interface UsageService {
    /**
     * 执行 {@code overview} 定义的接口操作。
     *
     * @return 操作结果。
     */
    Map<String, Object> overview();

    /**
     * 执行 {@code byDay} 定义的接口操作。
     *
     * @param days 方法参数。
     *
     * @return 操作结果。
     */
    List<Map<String, Object>> byDay(int days);

    /**
     * 执行 {@code byWeek} 定义的接口操作。
     *
     * @param weeks 方法参数。
     *
     * @return 操作结果。
     */
    List<Map<String, Object>> byWeek(int weeks);

    /**
     * 执行 {@code byMonth} 定义的接口操作。
     *
     * @param months 方法参数。
     *
     * @return 操作结果。
     */
    List<Map<String, Object>> byMonth(int months);

    /**
     * 执行 {@code byModel} 定义的接口操作。
     *
     * @return 操作结果。
     */
    List<Map<String, Object>> byModel();

    /**
     * 执行 {@code llmByDay} 定义的接口操作。
     *
     * @param days 方法参数。
     *
     * @return 操作结果。
     */
    List<Map<String, Object>> llmByDay(int days);

    /**
     * 执行 {@code llmByWeek} 定义的接口操作。
     *
     * @param weeks 方法参数。
     *
     * @return 操作结果。
     */
    List<Map<String, Object>> llmByWeek(int weeks);

    /**
     * 执行 {@code llmByMonth} 定义的接口操作。
     *
     * @param months 方法参数。
     *
     * @return 操作结果。
     */
    List<Map<String, Object>> llmByMonth(int months);

    /**
     * 执行 {@code embeddingOverview} 定义的接口操作。
     *
     * @return 操作结果。
     */
    Map<String, Object> embeddingOverview();
}
