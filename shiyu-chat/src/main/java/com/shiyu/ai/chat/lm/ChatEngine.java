package com.shiyu.ai.chat.lm;


import com.shiyu.ai.chat.lm.model.ModelAdapter;
import com.shiyu.ai.chat.lm.request.ModelRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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
    private Map<String, ModelAdapter> modelAdapterMap;

    /**
     * 模型适配器缓存（避免重复从 Map 中获取）
     */
    private final Map<ModelEnum, ModelAdapter> adapterCache = new ConcurrentHashMap<>();

    /**
     * 获取 ChatClient 实例
     * @param modelEnum 模型类型
     * @return ChatClient
     */
    public ChatClient getChatClient(ModelEnum modelEnum) {
        ModelAdapter adapter = getAdapter(modelEnum);
        return adapter.getChatClient();
    }

    /**
     * 同步调用模型
     * @param request 请求参数（包含 prompt、platform、modelName 等）
     * @return 模型响应
     */
    public String call(ModelRequest request) {
        ModelEnum modelEnum = resolveModelEnum(request);
        log.debug("Calling model: {} with input: {}", modelEnum, request.getPrompt());
        
        ModelAdapter adapter = getAdapter(modelEnum);
        String response = adapter.call(request);
        log.debug("Model: {} responded successfully", modelEnum);
        return response;
    }

    /**
     * 流式调用模型
     * @param request 请求参数（包含 prompt、platform、modelName 等）
     * @return 流式响应
     */
    public Flux<String> stream(ModelRequest request) {
        ModelEnum modelEnum = resolveModelEnum(request);
        log.debug("Streaming model: {} with input: {}", modelEnum, request.getPrompt());
        
        ModelAdapter adapter = getAdapter(modelEnum);
        Flux<String> response = adapter.stream(request);
        log.debug("Model: {} streaming started", modelEnum);
        return response;
    }

    /**
     * 根据 ModelRequest 解析 ModelEnum
     * 优先使用 platform 和 modelName 匹配，其次使用 meta 中的配置
     * @param request 请求参数
     * @return 模型类型
     */
    private ModelEnum resolveModelEnum(ModelRequest request) {
        // 如果有 platform 和 modelName，尝试匹配
        if (request.getPlatform() != null || request.getModelName() != null) {
            // 这里可以根据 platform 和 modelName 进行更精确的匹配
            // 暂时返回默认模型或根据现有逻辑推断
            return ModelEnum.fromAdapterName(request.getPlatform());
        }
        
        // 如果没有指定，使用默认模型
        return ModelEnum.defaultModel();
    }
    
    /**
     * 获取模型适配器（带缓存）
     * @param modelEnum 模型类型
     * @return 模型适配器
     */
    private ModelAdapter getAdapter(ModelEnum modelEnum) {
        return adapterCache.computeIfAbsent(modelEnum, key -> {
            ModelAdapter adapter = modelAdapterMap.get(key.getAdapterName());
            if (adapter == null) {
                log.error("No ModelAdapter found for: {}", key);
                throw new IllegalArgumentException("No ModelAdapter found for: " + key);
            }
            log.info("Loaded ModelAdapter for: {} -> {}", key, adapter.getClass().getSimpleName());
            return adapter;
        });
    }
}
