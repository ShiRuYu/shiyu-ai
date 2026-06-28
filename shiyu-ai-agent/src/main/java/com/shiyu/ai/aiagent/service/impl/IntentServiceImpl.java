package com.shiyu.ai.aiagent.service.impl;

import com.shiyu.ai.core.langchain4j.Lc4jModelManager;
import com.shiyu.ai.aiagent.langgraph4j.node.intent.IntentDefinition;
import com.shiyu.ai.aiagent.langgraph4j.node.intent.IntentDefinitionFactory;
import com.shiyu.ai.aiagent.service.IntentService;
import com.shiyu.ai.common.core.utils.JSONUtils;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
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
    private final Map<String, IntentAssistant> assistantCache = new ConcurrentHashMap<>();

    public IntentServiceImpl(Lc4jModelManager modelManager) {
        this.modelManager = modelManager;
    }

    @Override
    public IntentRecognitionResult recognize(String row, String column, String query, String platform, String modelName) {
        String effectiveRow = row != null ? row : "default";
        String effectiveColumn = column;

        // 根据 row + column 从工厂获取意图定义
        List<IntentDefinition> supportedIntents = IntentDefinitionFactory.getByCategory(effectiveRow, effectiveColumn);
        log.info("开始识别用户意图，row={}, column={}, 匹配 {} 个意图定义",
                effectiveRow, effectiveColumn, supportedIntents != null ? supportedIntents.size() : 0);

        try {
            String prompt = buildIntentPrompt(query, supportedIntents);

            String actualPlatform = platform != null ? platform : modelManager.getDefaultPlatform();
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
    public IntentRecognitionResult recognize(String row, String column, String query) {
        return recognize(row, column, query, null, null);
    }

    /**
     * 构建意图识别的 Prompt
     */
    private String buildIntentPrompt(String query, List<IntentDefinition> supportedIntents) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析以下用户输入的意图：\n\n");
        prompt.append("用户输入：").append(query).append("\n\n");

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

        // 判断是否有意图需要 slot 提取
        boolean needSlots = supportedIntents != null && supportedIntents.stream()
                .anyMatch(IntentDefinition::getRequireSlotFilling);

        // ---------- 需要槽位的意图：包含 slots 提取指令和 slot 定义 ----------
        if (needSlots) {
            prompt.append("部分意图包含槽位信息，需要从用户输入中提取。具体定义如下：\n");
            for (IntentDefinition intent : supportedIntents) {
                if (intent.getRequireSlotFilling() && intent.getSlots() != null && !intent.getSlots().isEmpty()) {
                    prompt.append("- ").append(intent.getCode())
                          .append(" 的槽位：").append(intent.getSlots()).append("\n");
                }
            }
            prompt.append("\n");

            prompt.append("""
                    请返回 JSON 格式结果，不要包含任何其他字符（包括 Markdown 标记、说明文字等）：
                    {
                      "intentCode": "WEATHER_QUERY",
                      "intentName": "天气查询",
                      "confidence": 0.95,
                      "slots": { "city": "北京", "date": "明天" }
                    }
                    注意：如果选择的意图不需要槽位，slots 字段固定为 {}。
                    """);
        } else {
            // ---------- 无需槽位的意图：简化输出，slots 固定为 {} ----------
            prompt.append("""
                    请返回 JSON 格式结果，不要包含任何其他字符（包括 Markdown 标记、说明文字等）：
                    {
                      "intentCode": "CHITCHAT",
                      "intentName": "闲聊",
                      "confidence": 0.95,
                      "slots": {}
                    }
                    """);
        }

        return prompt.toString();
    }

    /**
     * 解析意图识别响应
     */
    private IntentRecognitionResult parseIntentResponse(String response, List<IntentDefinition> supportedIntents) {
        log.debug("意图识别响应：{}", response);

        try {
            // 通用 JSON 提取：找到第一个 { 或 [，通过花括号匹配定位结尾
            // 无论外层包裹了什么（```json、<|begin_of_box|>、XML、纯文本等），都能正确提取
            String json = JSONUtils.extractJsonFragment(response);

            Map<String, Object> result = JSONUtils.parseObject(json, HashMap.class);

            String intentCode = (String) result.get("intentCode");
            String intentName = (String) result.get("intentName");
            Double confidence = parseDouble(result.get("confidence"));
            Object slotsObj = result.get("slots");
            Map<String, Object> slots = slotsObj instanceof Map
                    ? (Map<String, Object>) slotsObj
                    : new HashMap<>();

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
