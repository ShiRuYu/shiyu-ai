package com.shiyu.ai.agent.service.impl;

import com.shiyu.ai.agent.domain.Lc4jRequest;
import com.shiyu.ai.agent.domain.Lc4jResponse;
import com.shiyu.ai.agent.langchain4j.Lc4jModelManager;
import com.shiyu.ai.agent.service.Lc4jService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import static com.shiyu.ai.agent.service.impl.helper.Lc4jServiceHelper.*;

/**
 * LangChain4j 服务实现类
 * 基于 Lc4jModelManager 提供大模型调用能力
 * 使用 LangChain4j 的 AiServices 模式进行调用
 */
@Slf4j
@Service
public class Lc4jServiceImpl implements Lc4jService {
    
    private final Lc4jModelManager modelManager;
    
    public Lc4jServiceImpl(Lc4jModelManager modelManager) {
        this.modelManager = modelManager;
    }
    
    @Override
    public Lc4jResponse call(Lc4jRequest request) {
        log.info("收到对话请求：platform={}, model={}, prompt={}", 
                request.getPlatform(), request.getModel(), request.getPrompt());
        
        try {
            // 验证参数
            String validationError = validateRequest(request);
            if (validationError != null) {
                return buildErrorResponse(validationError, request.getPlatform(), request.getModel());
            }
            
            // 获取并验证同步模型
            ChatModel chatModel = validateChatModel(
                    modelManager.getChatModel(request.getPlatform(), request.getModel()),
                    request.getPlatform(), request.getModel());
            
            // 使用 AiServices 创建助手并进行调用
            String response = createAssistant(chatModel).chat(request.getPrompt());
            
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
            
            // 获取并验证流式模型
            StreamingChatModel streamingChatModel = validateStreamingChatModel(
                    modelManager.getStreamingChatModel(request.getPlatform(), request.getModel()),
                    request.getPlatform(), request.getModel());
            
            // 使用 AiServices 创建流式助手并返回 Flux
            return createStreamingAssistant(streamingChatModel).chat(request.getPrompt())
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
}
