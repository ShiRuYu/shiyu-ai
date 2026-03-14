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
    public Flux<String> stream(String text, 
                               @RequestParam(required = false, defaultValue = "SILICON_FLOW") String platformEnum) {
        return chatService.stream(text, platformEnum);
    }

    /**
     * 普通对话接口（基于 LiteFlow，支持多轮对话和记忆）
     */
    @PostMapping
    public Map<String, Object> chat(@RequestBody ChatRequest request) {
        return chatService.chat(request);
    }

    /**
     * GET 方式的对话接口（兼容旧版本）
     */
    @GetMapping
    public Map<String, Object> chatGet(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String userId) {
        ChatRequest request = new ChatRequest(text, sessionId, userId);
        return chatService.chat(request);
    }
}
