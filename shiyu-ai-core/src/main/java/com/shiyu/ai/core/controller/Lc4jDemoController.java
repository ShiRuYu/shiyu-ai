package com.shiyu.ai.core.controller;

import com.shiyu.ai.core.Lc4jRequest;
import com.shiyu.ai.core.Lc4jResponse;
import com.shiyu.ai.core.Lc4jService;
import com.shiyu.ai.core.langchain4j.Lc4jModelManager;
import com.shiyu.ai.common.core.api.Result;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LangChain4j 示例 Controller
 * 演示如何使用 LangChain4j 进行大模型调用
 */
@Slf4j
@RestController
@RequestMapping("/api/lc4j")
public class Lc4jDemoController {
    
    private final Lc4jService lc4jService;
    private final Lc4jModelManager modelManager;
    
    public Lc4jDemoController(Lc4jService lc4jService, Lc4jModelManager modelManager) {
        this.lc4jService = lc4jService;
        this.modelManager = modelManager;
    }
    
    @GetMapping("/platforms")
    public Result<List<String>> getAvailablePlatforms() {
        return Result.success(modelManager.getAvailablePlatforms());
    }
    
    /**
     * 同步对话接口
     * @param request 请求参数（platform、model、prompt）
     * @return AI 响应
     */
    @PostMapping("/chat")
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("收到聊天请求：platform={}, model={}, prompt={}", 
                request.getPlatform(), request.getModel(), request.getPrompt());
        
        try {
            // 转换为 Lc4jRequest
            Lc4jRequest lc4jRequest = Lc4jRequest.builder()
                    .platform(request.getPlatform())
                    .model(request.getModel())
                    .prompt(request.getPrompt())
                    .build();
            
            // 调用 Lc4jService
            Lc4jResponse lc4jResponse = lc4jService.call(lc4jRequest);
            
            log.info("模型响应成功");
            ChatResponse response = ChatResponse.builder()
                    .success(lc4jResponse.isSuccess())
                    .content(lc4jResponse.getContent())
                    .build();
            return Result.success(response);
                    
        } catch (Exception e) {
            log.error("模型调用失败", e);
            ChatResponse response = ChatResponse.builder()
                    .success(false)
                    .content("调用失败：" + e.getMessage())
                    .build();
            return Result.success(response);
        }
    }
    
    /**
     * 流式对话接口
     * @param request 请求参数
     * @return 流式响应
     */
    @PostMapping("/chat/stream")
    public Flux<String> streamChat(@Valid @RequestBody ChatRequest request) {
        log.info("收到流式聊天请求：platform={}, model={}, prompt={}", 
                request.getPlatform(), request.getModel(), request.getPrompt());
        
        try {
            // 转换为 Lc4jRequest
            Lc4jRequest lc4jRequest = Lc4jRequest.builder()
                    .platform(request.getPlatform())
                    .model(request.getModel())
                    .prompt(request.getPrompt())
                    .build();
            
            // 调用 Lc4jService 的流式方法
            return lc4jService.stream(lc4jRequest);
        } catch (Exception e) {
            log.error("流式对话处理失败", e);
            return Flux.error(e);
        }
    }
    
    @GetMapping("/default-model")
    public Result<Map<String, String>> getDefaultModel(@RequestParam String platform) {
        Map<String, String> result = new HashMap<>();
        result.put("platform", platform);
        result.put("defaultModel", modelManager.getDefaultModelName(platform));
        return Result.success(result);
    }
    
    /**
     * 聊天请求参数
     */
    @Data
    public static class ChatRequest {
        /**
         * 平台类型（OPENROUTER, OLLAMA, DEEPSEEK, OPENAI, SILICON_FLOW）
         */
        private String platform = "SILICON_FLOW";
        
        /**
         * 模型名称，为空时使用平台默认模型
         */
        private String model;
        
        /**
         * 用户输入的问题
         */
        private String prompt;
    }
    
    /**
     * 聊天响应
     */
    @Data
    @lombok.Builder
    public static class ChatResponse {
        /**
         * 是否成功
         */
        private boolean success;
        
        /**
         * AI 回复内容
         */
        private String content;
    }
}
