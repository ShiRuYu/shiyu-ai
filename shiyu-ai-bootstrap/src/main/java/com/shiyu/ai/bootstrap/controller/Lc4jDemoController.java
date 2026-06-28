package com.shiyu.ai.bootstrap.controller;

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
 * LangChain4j 绀轰緥 Controller
 * 婕旂ず濡備綍浣跨敤 LangChain4j 杩涜澶фā鍨嬭皟鐢?
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
     * 鍚屾瀵硅瘽鎺ュ彛
     * @param request 璇锋眰鍙傛暟锛坧latform銆乵odel銆乸rompt锛?
     * @return AI 鍝嶅簲
     */
    @PostMapping("/chat")
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("鏀跺埌鑱婂ぉ璇锋眰锛歱latform={}, model={}, prompt={}", 
                request.getPlatform(), request.getModel(), request.getPrompt());
        
        try {
            // 杞崲涓?Lc4jRequest
            Lc4jRequest lc4jRequest = Lc4jRequest.builder()
                    .platform(request.getPlatform())
                    .model(request.getModel())
                    .prompt(request.getPrompt())
                    .build();
            
            // 璋冪敤 Lc4jService
            Lc4jResponse lc4jResponse = lc4jService.call(lc4jRequest);
            
            log.info("妯″瀷鍝嶅簲鎴愬姛");
            ChatResponse response = ChatResponse.builder()
                    .success(lc4jResponse.isSuccess())
                    .content(lc4jResponse.getContent())
                    .build();
            return Result.success(response);
                    
        } catch (Exception e) {
            log.error("妯″瀷璋冪敤澶辫触", e);
            ChatResponse response = ChatResponse.builder()
                    .success(false)
                    .content("璋冪敤澶辫触锛? + e.getMessage())
                    .build();
            return Result.success(response);
        }
    }
    
    /**
     * 娴佸紡瀵硅瘽鎺ュ彛
     * @param request 璇锋眰鍙傛暟
     * @return 娴佸紡鍝嶅簲
     */
    @PostMapping("/chat/stream")
    public Flux<String> streamChat(@Valid @RequestBody ChatRequest request) {
        log.info("鏀跺埌娴佸紡鑱婂ぉ璇锋眰锛歱latform={}, model={}, prompt={}", 
                request.getPlatform(), request.getModel(), request.getPrompt());
        
        try {
            // 杞崲涓?Lc4jRequest
            Lc4jRequest lc4jRequest = Lc4jRequest.builder()
                    .platform(request.getPlatform())
                    .model(request.getModel())
                    .prompt(request.getPrompt())
                    .build();
            
            // 璋冪敤 Lc4jService 鐨勬祦寮忔柟娉?
            return lc4jService.stream(lc4jRequest);
        } catch (Exception e) {
            log.error("娴佸紡瀵硅瘽澶勭悊澶辫触", e);
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
     * 鑱婂ぉ璇锋眰鍙傛暟
     */
    @Data
    public static class ChatRequest {
        /**
         * 骞冲彴绫诲瀷锛圤PENROUTER, OLLAMA, DEEPSEEK, OPENAI, SILICON_FLOW锛?
         */
        private String platform = "SILICON_FLOW";
        
        /**
         * 妯″瀷鍚嶇О锛屼负绌烘椂浣跨敤骞冲彴榛樿妯″瀷
         */
        private String model;
        
        /**
         * 鐢ㄦ埛杈撳叆鐨勯棶棰?
         */
        private String prompt;
    }
    
    /**
     * 鑱婂ぉ鍝嶅簲
     */
    @Data
    @lombok.Builder
    public static class ChatResponse {
        /**
         * 鏄惁鎴愬姛
         */
        private boolean success;
        
        /**
         * AI 鍥炲鍐呭
         */
        private String content;
    }
}
