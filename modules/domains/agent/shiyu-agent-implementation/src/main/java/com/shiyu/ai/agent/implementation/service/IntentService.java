package com.shiyu.ai.agent.implementation.service;

import java.util.Map;

/** 意图识别服务接口 用于识别用户输入的真实意图 */
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
     * 意图识别结果
     *
     * @param success success 属性，表示该记录组件承载的数据。
     * @param intentCode intentCode 属性，表示该记录组件承载的数据。
     * @param intentName intentName 属性，表示该记录组件承载的数据。
     * @param confidence confidence 属性，表示该记录组件承载的数据。
     * @param slots slots 属性，表示该记录组件承载的数据。
     * @param errorMessage errorMessage 属性，表示该记录组件承载的数据。
     */
    record IntentRecognitionResult(
            boolean success,
            String intentCode,
            String intentName,
            Double confidence,
            Map<String, Object> slots,
            String errorMessage) {}
}
