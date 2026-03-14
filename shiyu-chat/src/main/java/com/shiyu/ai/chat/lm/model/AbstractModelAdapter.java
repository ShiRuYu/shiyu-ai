package com.shiyu.ai.chat.lm.model;

import com.shiyu.ai.chat.lm.ModelEnum;
import com.shiyu.ai.chat.lm.request.ModelRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象模型适配器基类
 * 提供通用的实现和错误处理逻辑，支持多模型缓存
 */
@Slf4j
public abstract class AbstractModelAdapter implements ModelAdapter {

    /**
     * ChatClient 缓存（按 modelName 缓存）
     */
    protected final Map<String, ChatClient> chatClientCache = new ConcurrentHashMap<>();

    /**
     * 获取 ChatClient 实例（由子类提供）
     * @param modelName 模型名称
     * @return ChatClient
     */
    protected abstract ChatClient doGetChatClient(String modelName);

    /**
     * 执行同步调用（由子类提供具体实现）
     */
    protected abstract String doCall(ChatClient client, ModelRequest request);

    /**
     * 执行流式调用（由子类提供具体实现）
     */
    protected abstract Flux<String> doStream(ChatClient client, ModelRequest request);

    @Override
    public ChatClient getChatClient() {
        return doGetChatClient(null);
    }
    
    /**
     * 根据请求获取 ChatClient 实例
     * @param request 请求参数
     * @return ChatClient
     */
    protected ChatClient getChatClient(ModelRequest request) {
        String modelName = request != null ? request.getModelName() : null;
        if (modelName == null || modelName.isEmpty()) {
            // 如果没有指定 modelName，使用默认配置
            return doGetChatClient(null);
        }
        
        // 从缓存中获取或创建 ChatClient
        return chatClientCache.computeIfAbsent(modelName, key -> {
            log.debug("Creating ChatClient for model: {}", key);
            return doGetChatClient(key);
        });
    }

    @Override
    public String call(ModelRequest request) {
        try {
            ChatClient client = getChatClient(request);
            if (client == null) {
                String modelName = request != null ? request.getModelName() : "default";
                log.warn("ChatClient is null for model: {}, using mock response", modelName);
                return buildMockResponse(request.getPrompt());
            }
            
            log.debug("Calling model: {} with prompt: {}", request.getModelName(), request.getPrompt());
            String response = doCall(client, request);
            log.debug("Model: {} responded successfully", request.getModelName());
            return response;
            
        } catch (Exception e) {
            log.error("Error calling model: {}. Error: {}", getType(), e.getMessage(), e);
            throw new RuntimeException("Failed to call model: " + getType().name(), e);
        }
    }

    @Override
    public Flux<String> stream(ModelRequest request) {
        try {
            ChatClient client = getChatClient(request);
            if (client == null) {
                String modelName = request != null ? request.getModelName() : "default";
                log.warn("ChatClient is null for model: {}, using mock stream", modelName);
                return buildMockStream(request.getPrompt());
            }
            
            log.debug("Streaming model: {} with prompt: {}", request.getModelName(), request.getPrompt());
            Flux<String> response = doStream(client, request);
            log.debug("Model: {} streaming started", request.getModelName());
            return response;
            
        } catch (Exception e) {
            log.error("Error streaming model: {}. Error: {}", getType(), e.getMessage(), e);
            return Flux.error(new RuntimeException("Failed to stream model: " + getType().name(), e));
        }
    }

    /**
     * 构建模拟响应（用于 ChatClient 为 null 的情况）
     */
    protected String buildMockResponse(String prompt) {
        return String.format("[%s] Mock response for: %s", getType().name(), prompt);
    }

    /**
     * 构建模拟流式响应
     */
    protected Flux<String> buildMockStream(String prompt) {
        return Flux.just(String.format("[%s] Mock stream for: %s", getType().name(), prompt));
    }
}
