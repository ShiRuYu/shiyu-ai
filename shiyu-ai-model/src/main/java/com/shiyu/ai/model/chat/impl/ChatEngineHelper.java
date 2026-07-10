package com.shiyu.ai.model.chat.impl;

import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import reactor.core.publisher.Flux;

public class ChatEngineHelper {

    public static String validateRequest(ChatRequest request) {
        if (request == null) {
            return "请求不能为空";
        }

        if (request.getPlatform() == null || request.getPlatform().trim().isEmpty()) {
            return "平台类型不能为空";
        }

        if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            return "问题内容不能为空";
        }

        return null;
    }

    public static ChatModel validateChatModel(ChatModel chatModel, String platform, String model) {
        if (chatModel == null) {
            throw new IllegalStateException("无法获取模型实例，请检查平台配置");
        }
        return chatModel;
    }

    public static StreamingChatModel validateStreamingChatModel(
            StreamingChatModel streamingChatModel, String platform, String model) {
        if (streamingChatModel == null) {
            throw new IllegalStateException("无法获取流式模型实例，请检查平台配置");
        }
        return streamingChatModel;
    }

    public static ChatResponse buildSuccessResponse(String content, String platform, String model) {
        return ChatResponse.builder()
                .success(true)
                .content(content)
                .platform(platform)
                .model(model)
                .build();
    }

    public static ChatResponse buildErrorResponse(String errorMessage, String platform, String model) {
        return ChatResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .platform(platform)
                .model(model)
                .build();
    }

    public static Assistant createAssistant(ChatModel chatModel) {
        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .build();
    }

    public static StreamingAssistant createStreamingAssistant(StreamingChatModel streamingChatModel) {
        return AiServices.builder(StreamingAssistant.class)
                .streamingChatModel(streamingChatModel)
                .build();
    }

    public static String getActualModelName(ChatRequest request, String defaultModelName) {
        if (request.getModel() != null && !request.getModel().trim().isEmpty()) {
            return request.getModel();
        }
        return defaultModelName;
    }

    public interface Assistant {
        String chat(String message);
    }

    public interface StreamingAssistant {
        Flux<String> chat(String message);
    }
}
