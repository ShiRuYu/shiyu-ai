package com.shiyu.ai.agent.agent.controller;

import com.shiyu.ai.agent.agent.domain.Lc4jRequest;
import com.shiyu.ai.agent.agent.domain.Lc4jResponse;
import com.shiyu.ai.agent.agent.service.Lc4jService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
    
    public Lc4jDemoController(Lc4jService lc4jService) {
        this.lc4jService = lc4jService;
    }
    
    /**
     * 获取可用的平台列表
     */
    @GetMapping("/platforms")
    public List<String> getAvailablePlatforms() {
        // TODO: 需要通过 Lc4jService 或其他方式获取平台列表
        return List.of("OPENROUTER", "OLLAMA", "DEEPSEEK", "OPENAI", "SILICON_FLOW");
    }
    
    /**
     * 同步对话接口
     * @param request 请求参数（platform、model、prompt）
     * @return AI 响应
     */
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
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
            return ChatResponse.builder()
                    .success(lc4jResponse.isSuccess())
                    .content(lc4jResponse.getContent())
                    .build();
                    
        } catch (Exception e) {
            log.error("模型调用失败", e);
            return ChatResponse.builder()
                    .success(false)
                    .content("调用失败：" + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 流式对话接口
     * @param request 请求参数
     * @return 流式响应
     */
    @PostMapping("/chat/stream")
    public Flux<String> streamChat(@RequestBody ChatRequest request) {
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
    
    /**
     * 获取指定平台的默认模型名称
     */
    @GetMapping("/default-model")
    public Map<String, String> getDefaultModel(@RequestParam String platform) {
        Map<String, String> result = new HashMap<>();
        // TODO: 需要通过 Lc4jService 或其他方式获取默认模型
        result.put("platform", platform);
        result.put("defaultModel", "未配置");
        return result;
    }
    
    /**
     * 聊天请求参数（使用动态配置）
     */
    @Data
    public static class ChatWithConfigRequest {
        /**
         * 平台类型（OPENROUTER, OLLAMA, DEEPSEEK, OPENAI, SILICON_FLOW）
         */
        private String platformType = "SILICON_FLOW";
        
        /**
         * Base URL
         */
        private String baseUrl;
        
        /**
         * API Key
         */
        private String apiKey;
        
        /**
         * 模型名称
         */
        private String modelName;
        
        /**
         * 温度参数（默认 0.7）
         */
        private Double temperature = 0.7;
        
        /**
         * 最大 Token 数（默认 4096）
         */
        private Integer maxTokens = 4096;
        
        /**
         * 最大重试次数（默认 3）
         */
        private Integer maxRetries = 3;
        
        /**
         * 用户输入的问题
         */
        private String prompt;
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
