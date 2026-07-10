package com.shiyu.ai.model.chat.impl;

import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatMemoryProvider;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import com.shiyu.ai.model.adapter.ModelManager;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.shiyu.ai.model.chat.impl.ChatEngineHelper.*;

@Slf4j
@Service
public class ChatEngineImpl implements ChatEngine {

    private final ModelManager modelManager;
    private final Map<String, Assistant> assistantCache = new ConcurrentHashMap<>();
    private final Map<String, StreamingAssistant> streamingAssistantCache = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private ChatMemoryProvider chatMemoryProvider;

    public ChatEngineImpl(ModelManager modelManager) {
        this.modelManager = modelManager;
    }

    private String assistantCacheKey(String platform, String model) {
        return platform + ":" + (model != null ? model : "");
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        log.info("收到对话请求：platform={}, model={}, prompt={}",
                request.getPlatform(), request.getModel(), request.getPrompt());

        try {
            String validationError = validateRequest(request);
            if (validationError != null) {
                return buildErrorResponse(validationError, request.getPlatform(), request.getModel());
            }

            ChatModel chatModel = validateChatModel(
                    modelManager.getChatModel(request.getPlatform(), request.getModel()),
                    request.getPlatform(), request.getModel());

            String cacheKey = assistantCacheKey(request.getPlatform(), request.getModel());
            Assistant assistant = assistantCache.computeIfAbsent(cacheKey,
                    k -> createAssistant(chatModel));

            String response = assistant.chat(request.getPrompt());

            log.info("模型响应成功");
            return buildSuccessResponse(response, request.getPlatform(),
                    getActualModelName(request, modelManager.getDefaultModelName(request.getPlatform())));

        } catch (Exception e) {
            log.error("模型调用失败", e);
            return buildErrorResponse("调用失败：" + e.getMessage(),
                    request.getPlatform(), request.getModel());
        }
    }

    @Override
    public Flux<ChatResponse> stream(ChatRequest request) {
        log.info("收到流式对话请求：platform={}, model={}, prompt={}",
                request.getPlatform(), request.getModel(), request.getPrompt());

        try {
            String validationError = validateRequest(request);
            if (validationError != null) {
                return Flux.error(new IllegalArgumentException(validationError));
            }

            StreamingChatModel streamingChatModel = validateStreamingChatModel(
                    modelManager.getStreamingChatModel(request.getPlatform(), request.getModel()),
                    request.getPlatform(), request.getModel());

            String cacheKey = assistantCacheKey(request.getPlatform(), request.getModel());
            StreamingAssistant streamingAssistant = streamingAssistantCache.computeIfAbsent(cacheKey,
                    k -> createStreamingAssistant(streamingChatModel));

            return streamingAssistant.chat(request.getPrompt())
                    .map(chunk -> ChatResponse.builder()
                            .success(true)
                            .content(chunk)
                            .platform(request.getPlatform())
                            .model(request.getModel())
                            .build())
                    .subscribeOn(Schedulers.boundedElastic());

        } catch (Exception e) {
            log.error("流式对话处理失败", e);
            return Flux.error(e);
        }
    }

    @Override
    public ChatResponse chatWithMemory(String sessionId, ChatRequest request) {
        if (chatMemoryProvider == null) {
            log.warn("ChatMemoryProvider 未配置，降级为普通 chat");
            return chat(request);
        }

        try {
            List<ChatMemoryProvider.ChatMessage> history = chatMemoryProvider.loadMemory(sessionId);
            String enrichedPrompt = buildEnrichedPrompt(history, request.getPrompt());

            ChatRequest enrichedRequest = ChatRequest.builder()
                    .platform(request.getPlatform())
                    .model(request.getModel())
                    .prompt(enrichedPrompt)
                    .chatType(request.getChatType())
                    .build();

            ChatResponse response = chat(enrichedRequest);

            if (response.isSuccess()) {
                List<ChatMemoryProvider.ChatMessage> updated = new ArrayList<>(history);
                updated.add(new ChatMemoryProvider.ChatMessage("user", request.getPrompt()));
                updated.add(new ChatMemoryProvider.ChatMessage("assistant", response.getContent()));
                chatMemoryProvider.saveMemory(sessionId, updated);
            }

            return response;
        } catch (Exception e) {
            log.error("带记忆的对话失败，降级为普通 chat", e);
            return chat(request);
        }
    }

    @Override
    public Flux<ChatResponse> streamWithMemory(String sessionId, ChatRequest request) {
        if (chatMemoryProvider == null) {
            log.warn("ChatMemoryProvider 未配置，降级为普通 stream");
            return stream(request);
        }

        try {
            List<ChatMemoryProvider.ChatMessage> history = chatMemoryProvider.loadMemory(sessionId);
            String enrichedPrompt = buildEnrichedPrompt(history, request.getPrompt());

            ChatRequest enrichedRequest = ChatRequest.builder()
                    .platform(request.getPlatform())
                    .model(request.getModel())
                    .prompt(enrichedPrompt)
                    .chatType(request.getChatType())
                    .build();

            StringBuilder fullContent = new StringBuilder();

            return stream(enrichedRequest)
                    .doOnNext(chunk -> {
                        if (chunk.getContent() != null) {
                            fullContent.append(chunk.getContent());
                        }
                    })
                    .doOnComplete(() -> {
                        List<ChatMemoryProvider.ChatMessage> updated = new ArrayList<>(history);
                        updated.add(new ChatMemoryProvider.ChatMessage("user", request.getPrompt()));
                        updated.add(new ChatMemoryProvider.ChatMessage("assistant", fullContent.toString()));
                        chatMemoryProvider.saveMemory(sessionId, updated);
                    });
        } catch (Exception e) {
            log.error("带记忆的流式对话失败，降级为普通 stream", e);
            return stream(request);
        }
    }

    private String buildEnrichedPrompt(List<ChatMemoryProvider.ChatMessage> history, String currentPrompt) {
        if (history == null || history.isEmpty()) {
            return currentPrompt;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("以下是之前的对话历史：\n");
        int start = Math.max(0, history.size() - 10);
        for (int i = start; i < history.size(); i++) {
            ChatMemoryProvider.ChatMessage msg = history.get(i);
            sb.append(msg.role()).append(": ").append(msg.content()).append("\n");
        }
        sb.append("\n用户当前问题: ").append(currentPrompt);
        return sb.toString();
    }
}
