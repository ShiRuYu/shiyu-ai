package com.shiyu.ai.agent.agent.service.impl;

import com.shiyu.ai.agent.langchain4j.Lc4jModelManager;
import com.shiyu.ai.agent.langgraph4j.node.intent.IntentDefinition;
import com.shiyu.ai.agent.agent.service.IntentService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 意图识别服务实现类
 * 使用 LLM 进行意图识别
 */
@Slf4j
@Service
public class IntentServiceImpl implements IntentService {
    
    private final Lc4jModelManager modelManager;
    
    public IntentServiceImpl(Lc4jModelManager modelManager) {
        this.modelManager = modelManager;
    }
    
    @Override
    public IntentRecognitionResult recognize(String userInput, List<IntentDefinition> supportedIntents) {
        log.info("开始识别用户意图，支持 {} 个预定义意图", supportedIntents != null ? supportedIntents.size() : 0);
        
        try {
            // 构建意图识别的 prompt
            String prompt = buildIntentPrompt(userInput, supportedIntents);
            
            // 获取默认模型进行意图识别
            ChatModel chatModel = modelManager.getChatModel(
                    "SILICON_FLOW", 
                    null);
            
            if (chatModel == null) {
                return new IntentRecognitionResult(
                    false, null, null, 0.0, Map.of(), 
                    "无法获取模型实例进行意图识别");
            }
            
            // 使用 AiServices 调用
            IntentAssistant assistant = AiServices.builder(IntentAssistant.class)
                    .chatModel(chatModel)
                    .build();
            
            String response = assistant.recognize(prompt);
            
            // 解析响应结果
            return parseIntentResponse(response, supportedIntents);
            
        } catch (Exception e) {
            log.error("意图识别失败", e);
            return new IntentRecognitionResult(
                false, null, null, 0.0, Map.of(), 
                "意图识别异常：" + e.getMessage());
        }
    }
    
    @Override
    public IntentRecognitionResult recognize(String userInput) {
        return recognize(userInput, null);
    }
    
    /**
     * 构建意图识别的 Prompt
     */
    private String buildIntentPrompt(String userInput, List<IntentDefinition> supportedIntents) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析以下用户输入的意图：\n\n");
        prompt.append("用户输入：").append(userInput).append("\n\n");
        
        if (supportedIntents != null && !supportedIntents.isEmpty()) {
            prompt.append("支持的意图列表：\n");
            for (IntentDefinition intent : supportedIntents) {
                prompt.append("- 代码：").append(intent.getCode())
                      .append(", 名称：").append(intent.getName())
                      .append(", 描述：").append(intent.getDescription())
                      .append(", 示例：").append(intent.getExamples() != null 
                          ? String.join(",", intent.getExamples()) : "无")
                      .append("\n");
            }
            prompt.append("\n");
        }
        
        prompt.append("请返回：\n");
        prompt.append("1. 最匹配的意图代码（intentCode）\n");
        prompt.append("2. 意图名称（intentName）\n");
        prompt.append("3. 置信度（confidence，0-1 之间的小数）\n");
        prompt.append("4. 提取的槽位信息（slots，JSON 格式）\n");
        prompt.append("\n请以 JSON 格式返回结果");
        
        return prompt.toString();
    }
    
    /**
     * 解析意图识别响应
     */
    private IntentRecognitionResult parseIntentResponse(String response, List<IntentDefinition> supportedIntents) {
        log.debug("意图识别响应：{}", response);
        
        try {
            // 简化处理：假设返回的是 JSON 格式
            // 实际项目中可以使用 JSON 解析库如 Jackson 或 Gson
            String json = response.trim();
            if (json.startsWith("```json")) {
                json = json.substring(7);
            }
            if (json.endsWith("```")) {
                json = json.substring(0, json.length() - 3);
            }
            json = json.trim();
            
            // 简单的 JSON 解析（生产环境建议使用 Jackson）
            Map<String, Object> result = parseSimpleJson(json);
            
            String intentCode = (String) result.get("intentCode");
            String intentName = (String) result.get("intentName");
            Double confidence = parseDouble(result.get("confidence"));
            Map<String, Object> slots = (Map<String, Object>) result.getOrDefault("slots", new HashMap<>());
            
            // 验证是否在支持的意图列表中
            if (supportedIntents != null && !supportedIntents.isEmpty()) {
                boolean isSupported = supportedIntents.stream()
                        .anyMatch(i -> i.getCode().equals(intentCode));
                if (!isSupported) {
                    log.warn("识别的意图 {} 不在支持的列表中", intentCode);
                    return new IntentRecognitionResult(
                        false, intentCode, intentName, confidence, slots,
                        "不支持的意图类型：" + intentCode);
                }
            }
            
            // 检查置信度阈值
            if (confidence < 0.5) {
                log.warn("意图识别置信度过低：{}", confidence);
                return new IntentRecognitionResult(
                    false, intentCode, intentName, confidence, slots,
                    "置信度过低：" + confidence);
            }
            
            log.info("意图识别成功：code={}, name={}, confidence={}", 
                    intentCode, intentName, confidence);
            
            return new IntentRecognitionResult(
                true, intentCode, intentName, confidence, slots, null);
                
        } catch (Exception e) {
            log.error("解析意图响应失败", e);
            return new IntentRecognitionResult(
                false, null, null, 0.0, Map.of(), 
                "响应解析失败：" + e.getMessage());
        }
    }
    
    /**
     * 简单的 JSON 解析（生产环境请替换为 Jackson/Gson）
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseSimpleJson(String json) {
        // 这里使用一个简化的实现
        // 实际项目应该使用 Jackson 的 ObjectMapper
        Map<String, Object> result = new HashMap<>();
        
        // 移除花括号
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
        }
        
        // 简单分割键值对
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replace("\"", "");
                String value = keyValue[1].trim().replace("\"", "");
                result.put(key, value);
            }
        }
        
        return result;
    }
    
    private Double parseDouble(Object obj) {
        if (obj instanceof Number number) {
            return number.doubleValue();
        }
        if (obj instanceof String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
    
    /**
     * 意图识别助手接口
     */
    interface IntentAssistant {
        String recognize(String prompt);
    }
}
