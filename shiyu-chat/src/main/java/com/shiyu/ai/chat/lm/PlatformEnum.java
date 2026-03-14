package com.shiyu.ai.chat.lm;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 模型平台枚举
 * 支持多种大语言模型提供商
 */
@Getter
@AllArgsConstructor
public enum PlatformEnum {

    /**
     * 本地部署的大模型
     */
    LOCAL("localModelAdapter"),

    /**
     * OpenAI 官方模型
     */
    OPENAI("openAIModelAdapter"),
    
    /**
     * OpenRouter 聚合平台模型
     */
    OPEN_ROUTER("openRouterModelAdapter"),
    
    /**
     * 硅基流动模型
     */
    SILICON_FLOW("siliconFlowModelAdapter"),

    /**
     * DeepSeek 深度求索模型
     */
    DEEPSEEK("deepseekModelAdapter")
    ;

    /**
     * ModelAdapter Bean 名称
     */
    private final String adapterName;

    /**
     * 枚举缓存（提高查找性能）
     */
    private static final Map<String, PlatformEnum> ENUM_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, PlatformEnum> ADAPTER_CACHE = new ConcurrentHashMap<>();

    /**
     * 根据枚举名称获取模型类型（不区分大小写）
     * @param enumName 枚举名称
     * @return 模型类型，未找到则返回 LOCAL
     */
    public static PlatformEnum fromEnumName(String enumName) {
        if (enumName == null || enumName.isBlank()) {
            return LOCAL;
        }
        
        return ENUM_CACHE.computeIfAbsent(enumName.toUpperCase(), key -> 
            Arrays.stream(values())
                .filter(platformEnum -> platformEnum.name().equalsIgnoreCase(key))
                .findFirst()
                .orElse(LOCAL)
        );
    }

    /**
     * 根据适配器名称获取模型类型（不区分大小写）
     * @param adapterName 适配器名称
     * @return 模型类型，未找到则返回 LOCAL
     */
    public static PlatformEnum fromAdapterName(String adapterName) {
        if (adapterName == null || adapterName.isBlank()) {
            return LOCAL;
        }

        return ADAPTER_CACHE.computeIfAbsent(adapterName.toLowerCase(), key ->
            Arrays.stream(values())
                .filter(platformEnum -> platformEnum.getAdapterName().equalsIgnoreCase(key))
                .findFirst()
                .orElse(LOCAL)
        );
    }
}
