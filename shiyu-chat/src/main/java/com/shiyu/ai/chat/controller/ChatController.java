package com.shiyu.ai.chat.controller;

import com.shiyu.ai.chat.domain.ChatRequest;
import com.shiyu.ai.chat.service.ChatService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private ChatService chatService;

    /**
     * 流式对话接口
     */
    @GetMapping("/stream")
    public Flux<String> stream(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false, defaultValue = "SILICON_FLOW") String platform,
            @RequestParam(required = false) String modelName) {
        ChatRequest request = new ChatRequest(text, sessionId, userId, platform, modelName);
        return chatService.stream(request);
    }

    /**
     * 普通对话接口（基于 LiteFlow，支持多轮对话和记忆）
     */
    @PostMapping
    public Map<String, Object> call(@RequestBody ChatRequest request) {
        return chatService.call(request);
    }

    /**
     * GET 方式的对话接口（兼容旧版本）
     */
    @GetMapping
    public Map<String, Object> callGet(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false, defaultValue = "SILICON_FLOW") String platform,
            @RequestParam(required = false) String modelName) {
        ChatRequest request = new ChatRequest(text, sessionId, userId, platform, modelName);
        return chatService.call(request);
    }
}
