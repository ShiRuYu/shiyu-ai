package com.shiyu.ai.chat.lm;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 大模型类型枚举
 * 支持多种大语言模型提供商
 */
@Getter
@AllArgsConstructor
public enum ModelEnum {

    /**
     * 本地部署的大模型
     */
    LOCAL(null, null, "localModelAdapter", "本地运行的大模型", true),

    /**
     * OpenAI 官方模型
     */
    OPENAI(null, null, "openAIModelAdapter", "OpenAI 官方模型", false),
    
    /**
     * OpenRouter 聚合平台模型
     */
    OPEN_ROUTER("openRouterChatModel", "openRouterChatClient", "openRouterModelAdapter", "OpenRouter 聚合模型", false),
    
    /**
     * 硅基流动模型
     */
    SILICON_FLOW("siliconFlowChatModel", "siliconFlowChatClient", "siliconFlowModelAdapter", "硅基流动模型", false),

    /**
     * DeepSeek 深度求索模型
     */
    DEEPSEEK(null, null, "deepseekModelAdapter", "DeepSeek 深度求索模型", false)
    ;

    /**
     * ChatModel Bean 名称
     */
    private final String chatModelName;
    
    /**
     * ChatClient Bean 名称
     */
    private final String chatClientName;
    
    /**
     * ModelAdapter Bean 名称
     */
    private final String adapterName;
    
    /**
     * 模型描述
     */
    private final String desc;
    
    /**
     * 是否为默认模型
     */
    private final boolean defaultModel;

    /**
     * 枚举缓存（提高查找性能）
     */
    private static final Map<String, ModelEnum> ENUM_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, ModelEnum> ADAPTER_CACHE = new ConcurrentHashMap<>();

    /**
     * 根据枚举名称获取模型类型（不区分大小写）
     * @param enumName 枚举名称
     * @return 模型类型，未找到则返回 LOCAL
     */
    public static ModelEnum fromEnumName(String enumName) {
        if (enumName == null || enumName.isBlank()) {
            return defaultModel();
        }
        
        return ENUM_CACHE.computeIfAbsent(enumName.toUpperCase(), key -> 
            Arrays.stream(values())
                .filter(modelEnum -> modelEnum.name().equalsIgnoreCase(key))
                .findFirst()
                .orElse(LOCAL)
        );
    }

    /**
     * 根据适配器名称获取模型类型（不区分大小写）
     * @param adapterName 适配器名称
     * @return 模型类型，未找到则返回默认模型
     */
    public static ModelEnum fromAdapterName(String adapterName) {
        if (adapterName == null || adapterName.isBlank()) {
            return defaultModel();
        }

        return ADAPTER_CACHE.computeIfAbsent(adapterName.toLowerCase(), key ->
            Arrays.stream(values())
                .filter(modelEnum -> modelEnum.getAdapterName().equalsIgnoreCase(key))
                .findFirst()
                .orElse(defaultModel())
        );
    }

    /**
     * 获取默认模型
     * @return 默认模型类型
     */
    public static ModelEnum defaultModel() {
        return Arrays.stream(values())
                .filter(modelEnum -> modelEnum.defaultModel)
                .findFirst()
                .orElse(LOCAL);
    }
    
    /**
     * 清除缓存（用于测试或重新加载配置）
     */
    public static void clearCache() {
        ENUM_CACHE.clear();
        ADAPTER_CACHE.clear();
    }
}

