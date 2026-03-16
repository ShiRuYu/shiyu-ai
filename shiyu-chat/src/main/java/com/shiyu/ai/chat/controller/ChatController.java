package com.shiyu.ai.chat.controller;

import com.shiyu.ai.chat.domain.ChatRequest;
import com.shiyu.ai.chat.lm.PlatformEnum;
import com.shiyu.ai.chat.service.ChatService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private ChatService chatService;

    /**
     * 流式对话接口
     */
    @PostMapping("/stream")
    public Flux<String> stream(@RequestBody ChatRequest request) {
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
     * 获取可用的 AI 平台及模型列表
     */
    @GetMapping("/platforms")
    public List<Map<String, Object>> getPlatforms() {
        List<Map<String, Object>> platforms = new ArrayList<>();
        for (PlatformEnum platform : PlatformEnum.values()) {
            Map<String, Object> platformInfo = new HashMap<>();
            platformInfo.put("name", platform.name());
            platformInfo.put("displayName", getDisplayName(platform));
            platforms.add(platformInfo);
        }
        return platforms;
    }

    /**
     * 获取指定平台的可用模型列表
     */
    @GetMapping("/platforms/{platform}/models")
    public List<Map<String, Object>> getModels(@PathVariable String platform) {
        // 这里可以根据实际平台配置返回具体模型列表
        // 目前返回默认模型配置
        List<Map<String, Object>> models = new ArrayList<>();
        Map<String, Object> modelInfo = new HashMap<>();
        modelInfo.put("name", getDefaultModelForPlatform(platform));
        modelInfo.put("displayName", "默认模型");
        models.add(modelInfo);
        return models;
    }

    /**
     * 获取平台显示名称
     */
    private String getDisplayName(PlatformEnum platform) {
        return switch (platform) {
            case OLLAMA -> "本地部署";
            case OPENAI -> "OpenAI";
            case OPEN_ROUTER -> "OpenRouter";
            case SILICON_FLOW -> "硅基流动";
            case DEEPSEEK -> "深度求索";
            default -> platform.name();
        };
    }

    /**
     * 获取平台默认模型名称
     */
    private String getDefaultModelForPlatform(String platform) {
        return switch (platform.toUpperCase()) {
            case "OLLAMA" -> "gemma3:4b";
            case "OPENAI" -> "gpt-4";
            case "OPEN_ROUTER" -> "gpt-4";
            case "SILICON_FLOW" -> "THUDM/GLM-Z1-9B-0414";
            case "DEEPSEEK" -> "deepseek-chat";
            default -> "default";
        };
    }
}
