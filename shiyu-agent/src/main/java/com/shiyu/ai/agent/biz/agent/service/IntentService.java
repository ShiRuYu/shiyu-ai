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
     * @param agentId   代理 ID（null 则使用 "default"）
     * @param category  意图分类
     * @param userInput 用户输入文本
     * @param platform  平台名称（null 则使用默认平台）
     * @return 意图识别结果
     */
    IntentRecognitionResult recognize(String agentId, String category, String userInput, String platform);

    /**
     * 识别用户意图（使用默认平台）
     *
     * @param agentId   代理 ID（null 则使用 "default"）
     * @param category  意图分类
     * @param userInput 用户输入文本
     * @return 意图识别结果
     */
    IntentRecognitionResult recognize(String agentId, String category, String userInput);

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
