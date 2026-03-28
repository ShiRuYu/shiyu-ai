package com.shiyu.ai.agent.service;

import com.shiyu.ai.agent.node.intent.IntentDefinition;

import java.util.List;
import java.util.Map;

/**
 * 意图识别服务接口
 * 用于识别用户输入的真实意图
 */
public interface IntentService {
    
    /**
     * 识别用户意图
     * @param userInput 用户输入文本
     * @param supportedIntents 支持的意图列表（可选）
     * @return 意图识别结果
     */
    IntentRecognitionResult recognize(String userInput, List<IntentDefinition> supportedIntents);
    
    /**
     * 识别用户意图（使用默认配置）
     * @param userInput 用户输入文本
     * @return 意图识别结果
     */
    IntentRecognitionResult recognize(String userInput);
    
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
