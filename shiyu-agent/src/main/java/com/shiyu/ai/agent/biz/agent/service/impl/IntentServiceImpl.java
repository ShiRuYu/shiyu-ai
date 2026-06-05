package com.shiyu.ai.agent.biz.agent.service.impl;

import com.shiyu.ai.agent.langchain4j.Lc4jModelManager;
import com.shiyu.ai.agent.langgraph4j.node.intent.IntentDefinition;
import com.shiyu.ai.agent.langgraph4j.node.intent.IntentDefinitionFactory;
import com.shiyu.ai.agent.biz.agent.service.IntentService;
import com.shiyu.ai.common.core.utils.JSONUtils;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 意图识别服务实现类
 * 使用 LLM 进行意图识别
 */
@Slf4j
@Service
public class IntentServiceImpl implements IntentService {

    private final Lc4jModelManager modelManager;

    @Value("${shiyu.ai.intent.platform:SILICON_FLOW}")
    private String defaultIntentPlatform;

    private final Map<String, IntentAssistant> assistantCache = new ConcurrentHashMap<>();

    public IntentServiceImpl(Lc4jModelManager modelManager) {
        this.modelManager = modelManager;
    }

    @Override
    public IntentRecognitionResult recognize(String row, String column, String userInput, String platform, String modelName) {
        String effectiveRow = row != null ? row : "default";
        String effectiveColumn = column;

        // 根据 row + column 从工厂获取意图定义
        List<IntentDefinition> supportedIntents = IntentDefinitionFactory.getByCategory(effectiveRow, effectiveColumn);
        log.info("开始识别用户意图，row={}, column={}, 匹配 {} 个意图定义",
                effectiveRow, effectiveColumn, supportedIntents != null ? supportedIntents.size() : 0);

        try {
            String prompt = buildIntentPrompt(userInput, supportedIntents);

            String actualPlatform = platform != null ? platform : defaultIntentPlatform;
            String actualModelName = modelName != null ? modelName : modelManager.getDefaultModelName(actualPlatform);
            String cacheKey = actualPlatform + ":" + actualModelName;

            ChatModel chatModel = modelManager.getChatModel(actualPlatform, actualModelName);
            if (chatModel == null) {
                return new IntentRecognitionResult(
                    false, null, null, 0.0, Map.of(),
                    "无法获取模型实例进行意图识别");
            }

            IntentAssistant assistant = assistantCache.computeIfAbsent(cacheKey,
                    k -> AiServices.builder(IntentAssistant.class).chatModel(chatModel).build());

            String response = assistant.recognize(prompt);
            return parseIntentResponse(response, supportedIntents);

        } catch (Exception e) {
            log.error("意图识别失败", e);
            return new IntentRecognitionResult(
                false, null, null, 0.0, Map.of(),
                "意图识别异常：" + e.getMessage());
        }
    }

    @Override
    public IntentRecognitionResult recognize(String row, String column, String userInput) {
        return recognize(row, column, userInput, null, null);
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

        prompt.append("""
                请返回：
                1. 最匹配的意图代码（intentCode）
                2. 意图名称（intentName）
                3. 置信度（confidence，0-1 之间的小数）
                4. 提取的槽位信息（slots，JSON 格式）

                请严格以 JSON 格式返回结果，不要包含任何其他字符（包括 Markdown 标记、说明文字等）。
                输出示例：
                {
                  "intentCode": "CHITCHAT",
                  "intentName": "闲聊",
                  "confidence": 0.1,
                  "slots": {}
                }
                """);

        return prompt.toString();
    }

    /**
     * 解析意图识别响应
     */
    private IntentRecognitionResult parseIntentResponse(String response, List<IntentDefinition> supportedIntents) {
        log.debug("意图识别响应：{}", response);

        try {
            // 清理：去除 Markdown 代码块标记
            String json = response.trim();
            if (json.startsWith("```json")) {
                json = json.substring(7);
            } else if (json.startsWith("```")) {
                json = json.substring(3);
            }
            if (json.endsWith("```")) {
                json = json.substring(0, json.length() - 3);
            }
            json = json.trim();

            // 清理：去除 <|begin_of_box|> / <|end_of_box|> 包裹
            if (json.startsWith("<|begin_of_box|>")) {
                json = json.substring("<|begin_of_box|>".length()).trim();
            }
            if (json.endsWith("<|end_of_box|>")) {
                json = json.substring(0, json.length() - "<|end_of_box|>".length()).trim();
            }

            Map<String, Object> result = JSONUtils.parseObject(json, HashMap.class);

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
