package com.shiyu.ai.chat.lm.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ModelEnum {

    LOCAL("localModelAdapter", "本地运行的大模型", true),
    OPENAI("openAIModelAdapter", "OpenAI 官方模型", false),
    DEEPSEEK("deepseekModelAdapter", "DeepSeek 模型", false)

    ;

    private final String beanName;
    private final String desc;
    private final boolean defaultModel;


    /**
     * 字符串 → Enum
     */
    public static ModelEnum fromName(String beanName) {
        if (beanName == null) return defaultModel();

        return Arrays.stream(values()).filter(modelEnum -> modelEnum.getBeanName().equalsIgnoreCase(beanName))
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

