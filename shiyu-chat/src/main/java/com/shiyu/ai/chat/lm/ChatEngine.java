package com.shiyu.ai.chat.lm;


import com.shiyu.ai.chat.lm.platform.PlatformAdapter;
import com.shiyu.ai.chat.lm.request.LmRequest;
import com.shiyu.ai.chat.lm.result.ChatResult;
import com.shiyu.ai.chat.lm.result.StreamResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天引擎服务
 * 提供统一的模型调用接口，支持同步和流式调用
 */
@Slf4j
@Service
public class ChatEngine {

    @Resource
    private Map<String, PlatformAdapter> modelAdapterMap;

    /**
     * 平台适配器缓存（避免重复从 Map 中获取）
     */
    private final Map<PlatformEnum, PlatformAdapter> adapterCache = new ConcurrentHashMap<>();

    /**
     * 同步调用模型
     * @param request 请求参数（包含 prompt、platform、modelName 等）
     * @return 模型响应（ChatResult）
     */
    public ChatResult call(LmRequest request) {
        PlatformEnum platformEnum = resolvePlatformEnum(request);
        log.info("[Sync Call] Source: {}, Platform: {}, Model: {}, Prompt: {}", 
                request.getSource() != null ? request.getSource() : "unknown",
                request.getPlatform() != null ? request.getPlatform() : "default",
                request.getModelName() != null ? request.getModelName() : "default",
                request.getPrompt());
        
        PlatformAdapter adapter = getAdapter(platformEnum);
        ChatResult response = adapter.call(request);
        log.debug("Model: {} responded successfully", platformEnum);
        return response;
    }

    /**
     * 流式调用模型
     * @param request 请求参数（包含 prompt、platform、modelName 等）
     * @return 流式响应（StreamResult）
     */
    public StreamResult stream(LmRequest request) {
        PlatformEnum platformEnum = resolvePlatformEnum(request);
        log.info("[Stream Call] Source: {}, Platform: {}, Model: {}, Prompt: {}", 
                request.getSource() != null ? request.getSource() : "unknown",
                request.getPlatform() != null ? request.getPlatform() : "default",
                request.getModelName() != null ? request.getModelName() : "default",
                request.getPrompt());
        
        PlatformAdapter adapter = getAdapter(platformEnum);
        StreamResult response = adapter.stream(request);
        log.debug("Model: {} streaming started", platformEnum);
        return response;
    }

    /**
     * 根据 LmRequest 解析 PlatformEnum
     * 优先使用 platform 和 modelName 匹配，其次使用 meta 中的配置
     * @param request 请求参数
     * @return 平台类型
     */
    private PlatformEnum resolvePlatformEnum(LmRequest request) {
        // 如果有 platform 和 modelName，尝试匹配
        if (request.getPlatform() != null || request.getModelName() != null) {
            // 这里可以根据 platform 和 modelName 进行更精确的匹配
            // 暂时返回默认模型或根据现有逻辑推断
            return PlatformEnum.fromAdapterName(request.getPlatform());
        }
        
        // 如果没有指定，使用 LOCAL 平台
        return PlatformEnum.OLLAMA;
    }
    
    /**
     * 获取模型适配器（带缓存）
     * @param platformEnum 模型类型
     * @return 模型适配器
     */
    private PlatformAdapter getAdapter(PlatformEnum platformEnum) {
        return adapterCache.computeIfAbsent(platformEnum, key -> {
            PlatformAdapter adapter = modelAdapterMap.get(key.getAdapterName());
            if (adapter == null) {
                log.error("No PlatformAdapter found for: {}", key);
                throw new IllegalArgumentException("No PlatformAdapter found for: " + key);
            }
            log.info("Loaded PlatformAdapter for: {} -> {}", key, adapter.getClass().getSimpleName());
            return adapter;
        });
    }
}
