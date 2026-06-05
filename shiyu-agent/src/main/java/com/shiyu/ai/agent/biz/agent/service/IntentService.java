package com.shiyu.ai.agent.biz.agent.service;

import java.util.Map;

/**
 * 意图识别服务接口
 * 用于识别用户输入的真实意图
 */
public interface IntentService {

    /**
     * 识别用户意图
     *
     * @param row       row key（agentId，null 则使用 "default"）
     * @param column    column key（意图分类）
     * @param userInput 用户输入文本
     * @param platform  平台名称（null 则使用默认平台）
     * @param modelName 模型名称（null 则使用默认模型）
     * @return 意图识别结果
     */
    IntentRecognitionResult recognize(String row, String column, String userInput, String platform, String modelName);

    /**
     * 识别用户意图（使用默认平台）
     *
     * @param row       row key（agentId，null 则使用 "default"）
     * @param column    column key（意图分类）
     * @param userInput 用户输入文本
     * @return 意图识别结果
     */
    IntentRecognitionResult recognize(String row, String column, String userInput);

    /**
     * 意图识别结果
     */
    record IntentRecognitionResult(
        boolean success,
        String intentCode,
        String intentName,
        Double confidence,
        Map<String, Object> slots,
        String errorMessage
    ) {}
}
