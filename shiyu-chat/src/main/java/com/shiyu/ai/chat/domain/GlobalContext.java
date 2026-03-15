package com.shiyu.ai.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Data
public class GlobalContext {
    private Map<String, Object> contextMap = new HashMap<>();

    public void set(String key, Object value) {
        contextMap.put(key, value);
    }

    public <T> T get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        if (contextMap == null) {
            return null;
        }

        try {
            return (T) contextMap.get(key);
        } catch (ClassCastException e) {
            throw new ClassCastException("Value for key '" + key +
                    "' is not of expected type");
        }
    }

    public <T> T get(String key, T defaultValue) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        if (contextMap == null) {
            return defaultValue;
        }

        Object value = contextMap.get(key);
        return value != null ? (T) value : defaultValue;
    }

    public boolean contains(String key) {
        return contextMap.containsKey(key);
    }

    public Map<String, Object> getAll() {
        return contextMap;
    }

    @Getter
    @AllArgsConstructor
    public enum ChatBizKeyEnum {
        QUERY("query", "用户输入"),
        FINAL_ANSWER("finalAnswer", "最终回答"),
        INTENT("intent", "意图"),
        CHAIN("chain", "触发的链"),
        TOT_THOUGHT_NODES("thoughtNodes", "ToT 流程候选方案"),
        TOT_FINAL_THOUGHT("finalThought", "ToT 最终方案"),
                    
        // 记忆相关
        SESSION_ID("sessionId", "会话 ID"),
        USER_ID("userId", "用户 ID"),
        MEMORY_CONTEXT("memoryContext", "记忆上下文"),
        CONVERSATION_HISTORY("conversationHistory", "对话历史"),
        
        // 平台和模型配置
        PLATFORM("platform", "平台名称"),
        MODEL_NAME("modelName", "模型名称"),
        STREAM_FLUX("streamFlux", "流式响应 Flux"),
        STREAM_MODE("streamMode", "流式模式标志");

        private final String code;
        private final String desc;
    }
}
