package com.shiyu.ai.agent.service.impl;

import com.shiyu.ai.agent.langchain4j.Lc4jModelManager;
import com.shiyu.ai.agent.domain.Lc4jRequest;
import com.shiyu.ai.agent.domain.Lc4jResponse;
import com.shiyu.ai.agent.service.Lc4jService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

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
            if (request.getPlatform() == null || request.getPlatform().trim().isEmpty()) {
                return Lc4jResponse.builder()
                        .success(false).errorMessage("平台类型不能为空").build();
            }
            
            if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
                return Lc4jResponse.builder()
                        .success(false).errorMessage("问题内容不能为空").build();
            }
            
            // 获取同步模型
            ChatModel chatModel = modelManager.getChatModel(
                    request.getPlatform(), request.getModel());
            
            if (chatModel == null) {
                return Lc4jResponse.builder().success(false)
                        .errorMessage("无法获取模型实例，请检查平台配置")
                        .platform(request.getPlatform()).model(request.getModel()).build();
            }
            
            // 使用 AiServices 创建助手并进行调用
            Assistant assistant = AiServices.builder(Assistant.class)
                    .chatModel(chatModel).build();
            
            String response = assistant.chat(request.getPrompt());
            
            log.info("模型响应成功");
            return Lc4jResponse.builder().success(true).content(response)
                    .platform(request.getPlatform()).model(getActualModelName(request)).build();
                    
        } catch (Exception e) {
            log.error("模型调用失败", e);
            return Lc4jResponse.builder().success(false)
                    .errorMessage("调用失败：" + e.getMessage())
                    .platform(request.getPlatform()).model(request.getModel()).build();
        }
    }
    
    @Override
    public Flux<String> stream(Lc4jRequest request) {
        log.info("收到流式对话请求：platform={}, model={}, prompt={}", 
                request.getPlatform(), request.getModel(), request.getPrompt());
        
        try {
            // 验证参数
            if (request.getPlatform() == null || request.getPlatform().trim().isEmpty()) {
                return Flux.error(new IllegalArgumentException("平台类型不能为空"));
            }
            
            if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
                return Flux.error(new IllegalArgumentException("问题内容不能为空"));
            }
            
            // 获取流式模型
            StreamingChatModel streamingChatModel = modelManager.getStreamingChatModel(
                    request.getPlatform(), request.getModel());
            
            if (streamingChatModel == null) {
                return Flux.error(new RuntimeException("无法获取流式模型实例，请检查平台配置"));
            }
            
            // 使用 AiServices 创建流式助手 - 接口返回 Flux<String>
            StreamingAssistant streamingAssistant = AiServices.builder(StreamingAssistant.class)
                    .streamingChatModel(streamingChatModel).build();
            
            // 直接返回 Flux，AiServices 会自动处理流式输出
            return streamingAssistant.chat(request.getPrompt())
                    .subscribeOn(Schedulers.boundedElastic());
                
        } catch (Exception e) {
            log.error("流式对话处理失败", e);
            return Flux.error(e);
        }
    }
    
    /**
     * 流式执行 LLM 调用（用于节点调用）
     * @param request 对话请求
     * @return 流式响应
     */
    public Flux<Lc4jResponse> streamResponse(Lc4jRequest request) {
        return stream(request)
                .map(content -> Lc4jResponse.builder()
                        .success(true)
                        .content(content)
                        .platform(request.getPlatform())
                        .model(getActualModelName(request))
                        .build())
                .onErrorResume(e -> {
                    log.error("流式调用失败", e);
                    return Flux.just(Lc4jResponse.builder()
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build());
                });
    }
    
    /**
     * 获取实际使用的模型名称
     */
    private String getActualModelName(Lc4jRequest request) {
        if (request.getModel() != null && !request.getModel().trim().isEmpty()) {
            return request.getModel();
        }
        return modelManager.getDefaultModelName(request.getPlatform());
    }
    
    /**
     * 定义一个助手接口，用于同步对话
     * AiServices 会自动实现此接口
     */
    interface Assistant {
        String chat(String message);
    }
    
    /**
     * 定义一个流式助手接口，用于流式对话
     * AiServices 会自动实现此接口，返回 Flux<String>
     */
    interface StreamingAssistant {
        Flux<String> chat(String message);
    }
}
