package com.shiyu.ai.agent.biz.agent.service.impl;

import com.shiyu.ai.agent.biz.agent.domain.Lc4jRequest;
import com.shiyu.ai.agent.biz.agent.domain.Lc4jResponse;
import com.shiyu.ai.agent.langchain4j.Lc4jModelManager;
import com.shiyu.ai.agent.biz.agent.service.Lc4jService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.shiyu.ai.agent.biz.agent.service.impl.helper.Lc4jServiceHelper.*;

/**
 * LangChain4j 服务实现类
 * 基于 Lc4jModelManager 提供大模型调用能力
 * 使用 LangChain4j 的 AiServices 模式进行调用
 */
@Slf4j
@Service
public class Lc4jServiceImpl implements Lc4jService {

    private final Lc4jModelManager modelManager;
    private final Map<String, Assistant> assistantCache = new ConcurrentHashMap<>();
    private final Map<String, StreamingAssistant> streamingAssistantCache = new ConcurrentHashMap<>();

    public Lc4jServiceImpl(Lc4jModelManager modelManager) {
        this.modelManager = modelManager;
    }

    private String assistantCacheKey(String platform, String model) {
        return platform + ":" + (model != null ? model : "");
    }

    @Override
    public Lc4jResponse call(Lc4jRequest request) {
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
    public Flux<String> stream(Lc4jRequest request) {
        log.info("收到流式对话请求：platform={}, model={}, prompt={}", 
                request.getPlatform(), request.getModel(), request.getPrompt());
        
        try {
            // 验证参数
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
                    .subscribeOn(Schedulers.boundedElastic());
                
        } catch (Exception e) {
            log.error("流式对话处理失败", e);
            return Flux.error(e);
        }
    }
    
    @Override
    public ChatModel getChatModel(String platformType, String modelName) {
        log.debug("获取 ChatModel：platform={}, model={}", platformType, modelName);
        return modelManager.getChatModel(platformType, modelName);
    }
    
    @Override
    public StreamingChatModel getStreamingChatModel(String platformType, String modelName) {
        log.debug("获取 StreamingChatModel：platform={}, model={}", platformType, modelName);
        return modelManager.getStreamingChatModel(platformType, modelName);
    }

    @Override
    public String getDefaultPlatform() {
        return modelManager.getDefaultPlatform();
    }

    @Override
    public String getDefaultModelName(String platformType) {
        return modelManager.getDefaultModelName(platformType);
    }
}
