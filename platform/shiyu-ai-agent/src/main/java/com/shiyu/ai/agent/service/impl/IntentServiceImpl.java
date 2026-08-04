package com.shiyu.ai.agent.service.impl;

import com.shiyu.ai.model.adapter.ModelManager;
import com.shiyu.ai.agent.node.intent.IntentDefinition;
import com.shiyu.ai.agent.node.intent.IntentDefinitionFactory;
import com.shiyu.ai.agent.service.IntentService;
import com.shiyu.ai.common.core.utils.JSONUtils;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class IntentServiceImpl implements IntentService {

    private final ModelManager modelManager;
    private final Map<String, IntentAssistant> assistantCache = new ConcurrentHashMap<>();

    public IntentServiceImpl(ModelManager modelManager) {
        this.modelManager = modelManager;
    }

    @Override
    public IntentRecognitionResult recognize(String row, String column, String query, String platform, String modelName) {
        String effectiveRow = row != null ? row : "default";
        String effectiveColumn = column;

        List<IntentDefinition> supportedIntents = IntentDefinitionFactory.getByCategory(effectiveRow, effectiveColumn);
        log.info("Recognizing user intent: row={}, column={}, matched {} intents",
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
                    "Unable to obtain model instance");
            }

            IntentAssistant assistant = assistantCache.computeIfAbsent(cacheKey,
                    k -> AiServices.builder(IntentAssistant.class).chatModel(chatModel).build());

            String response = assistant.recognize(prompt);
            return parseIntentResponse(response, supportedIntents);

        } catch (Exception e) {
            log.error("Intent recognition failed", e);
            return new IntentRecognitionResult(
                false, null, null, 0.0, Map.of(),
                "Intent recognition error: " + e.getMessage());
        }
    }

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
                prompt.append("- Code: ").append(intent.getCode())
                      .append(", Name: ").append(intent.getName())
                      .append(", Description: ").append(intent.getDescription())
                      .append(", Examples: ").append(intent.getExamples() != null
                          ? String.join(",", intent.getExamples()) : "N/A")
                      .append("\n");
            }
            prompt.append("\n");
        }

        boolean needSlots = supportedIntents != null && supportedIntents.stream()
                .anyMatch(IntentDefinition::getRequireSlotFilling);

        if (needSlots) {
            prompt.append("Some intents require slot extraction from user input:\n");
            for (IntentDefinition intent : supportedIntents) {
                if (intent.getRequireSlotFilling() && intent.getSlots() != null && !intent.getSlots().isEmpty()) {
                    prompt.append("- ").append(intent.getCode())
                          .append(" slots: ").append(intent.getSlots()).append("\n");
                }
            }
            prompt.append("\n");

            prompt.append("""
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
            prompt.append("""
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

    private IntentRecognitionResult parseIntentResponse(String response, List<IntentDefinition> supportedIntents) {
        log.debug("Intent recognition response: {}", response);

        try {
            String json = JSONUtils.extractJsonFragment(response);

            Map<String, Object> result = JSONUtils.parseObject(json, HashMap.class);

            String intentCode = (String) result.get("intentCode");
            String intentName = (String) result.get("intentName");
            Double confidence = parseDouble(result.get("confidence"));
            Object slotsObj = result.get("slots");
            Map<String, Object> slots = slotsObj instanceof Map
                    ? (Map<String, Object>) slotsObj
                    : new HashMap<>();

            if (supportedIntents != null && !supportedIntents.isEmpty()) {
                boolean isSupported = supportedIntents.stream()
                        .anyMatch(i -> i.getCode().equals(intentCode));
                if (!isSupported) {
                    log.warn("Recognized intent {} not in supported list", intentCode);
                    return new IntentRecognitionResult(
                        false, intentCode, intentName, confidence, slots,
                        "Unsupported intent type: " + intentCode);
                }
            }

            if (confidence < 0.5) {
                log.warn("Low confidence: {}", confidence);
                return new IntentRecognitionResult(
                    false, intentCode, intentName, confidence, slots,
                    "Low confidence: " + confidence);
            }

            log.info("Intent success: code={}, name={}, confidence={}",
                    intentCode, intentName, confidence);

            return new IntentRecognitionResult(
                true, intentCode, intentName, confidence, slots, null);

        } catch (Exception e) {
            log.error("Parse intent response failed", e);
            return new IntentRecognitionResult(
                false, null, null, 0.0, Map.of(),
                "Parse failed: " + e.getMessage());
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

    interface IntentAssistant {
        String recognize(String prompt);
    }
}
