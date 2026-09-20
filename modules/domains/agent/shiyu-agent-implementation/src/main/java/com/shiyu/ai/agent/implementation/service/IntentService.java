package com.shiyu.ai.agent.implementation.service;

import java.util.Map;

/**
 * 提供 Intent 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface IntentService {

    /**
     * 识别用户意图
     *
     * @param row row key（agentId，null 则使用 "default"）
     * @param column column key（意图分类）
     * @param query 用户输入文本
     * @param platform 平台名称（null 则使用默认平台）
     * @param modelName 模型名称（null 则使用默认模型）
     * @return 意图识别结果
     */
    IntentRecognitionResult recognize(
            String row, String column, String query, String platform, String modelName);

    /**
     * 识别用户意图（使用默认平台）
     *
     * @param row row key（agentId，null 则使用 "default"）
     * @param column column key（意图分类）
     * @param query 用户输入文本
     * @return 意图识别结果
     */
    IntentRecognitionResult recognize(String row, String column, String query);

    /**
     * 封装 Intent Recognition 相关的不可变数据及其字段约束。
     */
    record IntentRecognitionResult(
            boolean success,
            String intentCode,
            String intentName,
            Double confidence,
            Map<String, Object> slots,
            String errorMessage) {}
}
