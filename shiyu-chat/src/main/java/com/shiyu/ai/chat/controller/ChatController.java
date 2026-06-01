package com.shiyu.ai.chat.controller;

import com.shiyu.ai.chat.config.PlatformProperties;
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

    @Resource
    private PlatformProperties platformProperties;

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

    @GetMapping("/platforms/{platform}/models")
    public List<Map<String, Object>> getModels(@PathVariable String platform) {
        List<Map<String, Object>> models = new ArrayList<>();
        Map<String, Object> modelInfo = new HashMap<>();
        modelInfo.put("name", resolveDefaultModel(platform));
        modelInfo.put("displayName", "默认模型");
        models.add(modelInfo);
        return models;
    }

    private String resolveDefaultModel(String platform) {
        if (platform == null) return platformProperties.getSiliconflow().getModel();
        return switch (platform.toUpperCase()) {
            case "OLLAMA" -> platformProperties.getOllama().getModel();
            case "DEEPSEEK" -> platformProperties.getDeepseek().getModel();
            case "OPENAI" -> platformProperties.getOpenai().getModel();
            case "OPEN_ROUTER" -> platformProperties.getOpenrouter().getModel();
            case "SILICON_FLOW" -> platformProperties.getSiliconflow().getModel();
            default -> platformProperties.getSiliconflow().getModel();
        };
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

}
