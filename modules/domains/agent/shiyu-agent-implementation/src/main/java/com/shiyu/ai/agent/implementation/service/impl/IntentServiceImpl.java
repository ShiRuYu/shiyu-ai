package com.shiyu.ai.agent.implementation.service.impl;

import com.shiyu.ai.agent.implementation.node.intent.IntentDefinition;
import com.shiyu.ai.agent.implementation.node.intent.IntentDefinitionFactory;
import com.shiyu.ai.agent.implementation.service.IntentService;
import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.model.contract.api.ChatEngine;
import com.shiyu.ai.model.contract.model.ChatMessage;
import com.shiyu.ai.model.contract.model.ChatRequest;
import com.shiyu.ai.model.contract.model.ChatResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提供 Intent 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Slf4j
@Service
public class IntentServiceImpl implements IntentService {

    /**
     * chatEngine 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ChatEngine chatEngine;

    /**
     * 执行 Intent 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param chatEngine 用于完成本次业务处理的 chatEngine 参数。
     */
    public IntentServiceImpl(ChatEngine chatEngine) {
        this.chatEngine = chatEngine;
    }

    /**
     * 执行 Intent 相关业务数据，并返回处理结果。
     *
     * @param row 用于完成本次业务处理的 row 参数。
     * @param column 用于完成本次业务处理的 column 参数。
     * @param query 用于筛选目标数据的查询条件。
     * @param platform 用于完成本次业务处理的 platform 参数。
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 Intent 相关操作生成的结果数据。
     */
    @Override
    public IntentRecognitionResult recognize(
            String row, String column, String query, String platform, String modelName) {
        String effectiveRow = row != null ? row : "default";
        String effectiveColumn = column;

        List<IntentDefinition> supportedIntents =
                IntentDefinitionFactory.getByCategory(effectiveRow, effectiveColumn);
        log.info(
                "Recognizing user intent: row={}, column={}, matched {} intents",
                effectiveRow,
                effectiveColumn,
                supportedIntents != null ? supportedIntents.size() : 0);

        try {
            String prompt = buildIntentPrompt(query, supportedIntents);

            String actualPlatform =
                    platform != null && !platform.isBlank() ? platform : "SILICON_FLOW";
            ChatResponse response =
                    chatEngine.chat(
                            ChatRequest.builder()
                                    .platform(actualPlatform)
                                    .model(modelName)
                                    .messages(List.of(ChatMessage.text("user", prompt)))
                                    .build());
            if (response == null || !response.isSuccess()) {
                return new IntentRecognitionResult(
                        false,
                        null,
                        null,
                        0.0,
                        Map.of(),
                        response == null
                                ? "Unable to obtain model response"
                                : response.getErrorMessage());
            }
            return parseIntentResponse(response.getContent(), supportedIntents);

        } catch (Exception e) {
            log.error("Intent recognition failed", e);
            return new IntentRecognitionResult(
                    false,
                    null,
                    null,
                    0.0,
                    Map.of(),
                    "Intent recognition error: " + e.getMessage());
        }
    }

    /**
     * 执行 Intent 相关业务数据，并返回处理结果。
     *
     * @param row 用于完成本次业务处理的 row 参数。
     * @param column 用于完成本次业务处理的 column 参数。
     * @param query 用于筛选目标数据的查询条件。
     * @return 返回 Intent 相关操作生成的结果数据。
     */
    @Override
    public IntentRecognitionResult recognize(String row, String column, String query) {
        return recognize(row, column, query, null, null);
    }

    private String buildIntentPrompt(String query, List<IntentDefinition> supportedIntents) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Please analyze the user input intent:\n\n");
        prompt.append("User input: ").append(query).append("\n\n");

        if (supportedIntents != null && !supportedIntents.isEmpty()) {
            prompt.append("Supported intents:\n");
            for (IntentDefinition intent : supportedIntents) {
                prompt.append("- Code: ")
                        .append(intent.getCode())
                        .append(", Name: ")
                        .append(intent.getName())
                        .append(", Description: ")
                        .append(intent.getDescription())
                        .append(", Examples: ")
                        .append(
                                intent.getExamples() != null
                                        ? String.join(",", intent.getExamples())
                                        : "N/A")
                        .append("\n");
            }
            prompt.append("\n");
        }

        boolean needSlots =
                supportedIntents != null
                        && supportedIntents.stream()
                                .anyMatch(IntentDefinition::getRequireSlotFilling);

        if (needSlots) {
            prompt.append("Some intents require slot extraction from user input:\n");
            for (IntentDefinition intent : supportedIntents) {
                if (intent.getRequireSlotFilling()
                        && intent.getSlots() != null
                        && !intent.getSlots().isEmpty()) {
                    prompt.append("- ")
                            .append(intent.getCode())
                            .append(" slots: ")
                            .append(intent.getSlots())
                            .append("\n");
                }
            }
            prompt.append("\n");

            prompt.append(
                    """
                    Return result as JSON only:
                    {
                      "intentCode": "WEATHER_QUERY",
                      "intentName": "Weather Query",
                      "confidence": 0.95,
                      "slots": { "city": "Beijing", "date": "tomorrow" }
                    }
                    Note: slots must be {} if selected intent does not need slots.
                    """);
        } else {
            prompt.append(
                    """
                    Return result as JSON only:
                    {
                      "intentCode": "CHITCHAT",
                      "intentName": "Chat",
                      "confidence": 0.95,
                      "slots": {}
                    }
                    """);
        }

        return prompt.toString();
    }

    @SuppressWarnings("unchecked")
    private IntentRecognitionResult parseIntentResponse(
            String response, List<IntentDefinition> supportedIntents) {
        log.debug("Intent recognition response: {}", response);

        try {
            String json = JSONUtils.extractJsonFragment(response);

            Map<String, Object> result = JSONUtils.parseMap(json);

            String intentCode = (String) result.get("intentCode");
            String intentName = (String) result.get("intentName");
            Double confidence = parseDouble(result.get("confidence"));
            Object slotsObj = result.get("slots");
            Map<String, Object> slots =
                    slotsObj instanceof Map ? (Map<String, Object>) slotsObj : new HashMap<>();

            if (supportedIntents != null && !supportedIntents.isEmpty()) {
                boolean isSupported =
                        supportedIntents.stream().anyMatch(i -> i.getCode().equals(intentCode));
                if (!isSupported) {
                    log.warn("Recognized intent {} not in supported list", intentCode);
                    return new IntentRecognitionResult(
                            false,
                            intentCode,
                            intentName,
                            confidence,
                            slots,
                            "Unsupported intent type: " + intentCode);
                }
            }

            if (confidence < 0.5) {
                log.warn("Low confidence: {}", confidence);
                return new IntentRecognitionResult(
                        false,
                        intentCode,
                        intentName,
                        confidence,
                        slots,
                        "Low confidence: " + confidence);
            }

            log.info(
                    "Intent success: code={}, name={}, confidence={}",
                    intentCode,
                    intentName,
                    confidence);

            return new IntentRecognitionResult(
                    true, intentCode, intentName, confidence, slots, null);

        } catch (Exception e) {
            log.error("Parse intent response failed", e);
            return new IntentRecognitionResult(
                    false, null, null, 0.0, Map.of(), "Parse failed: " + e.getMessage());
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
}
