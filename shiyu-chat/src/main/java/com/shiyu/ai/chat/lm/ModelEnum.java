package com.shiyu.ai.chat.lm;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ModelEnum {

    LOCAL(null,null,"localModelAdapter", "本地运行的大模型", true),

    OPENAI(null,null,"openAIModelAdapter", "OpenAI 官方模型", false),
    OPEN_ROUTER("openRouterChatModel","openRouterChatClient","openRouterModelAdapter", "openRouter模型", false),
    SILICON_FLOW("siliconFlowChatModel","siliconFlowChatClient","siliconFlowModelAdapter", "硅基流动模型", false),

    DEEPSEEK(null,null,"deepseekModelAdapter", "DeepSeek 模型", false)

    ;

    private final String chatModelName;
    private final String chatClientName;
    private final String adapterName;
    private final String desc;
    private final boolean defaultModel;

    /**
     * 根据枚举name获取
     */
    public static ModelEnum fromEnumName(String enumName) {
        return Arrays.stream(values())
                .filter(modelEnum -> modelEnum.name().equalsIgnoreCase(enumName))
                .findFirst()
                .orElse(LOCAL);
    }

    /**
     * 字符串 → Enum
     */
    public static ModelEnum fromAdapterName(String adapterName) {
        if (adapterName == null) return defaultModel();

        return Arrays.stream(values())
                .filter(modelEnum -> modelEnum.getAdapterName().equalsIgnoreCase(adapterName))
                .findFirst()
                .orElse(defaultModel());
    }

    /**
     * 获取默认模型
     */
    public static ModelEnum defaultModel() {
        return Arrays.stream(values()).filter(modelEnum -> modelEnum.defaultModel)
                .findFirst()
                .orElse(LOCAL);
    }
}

