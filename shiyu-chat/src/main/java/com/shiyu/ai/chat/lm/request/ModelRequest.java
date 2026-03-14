package com.shiyu.ai.chat.lm.request;

import lombok.Data;
import org.springframework.ai.chat.messages.Message;

import java.util.List;
import java.util.Map;

/**
 * 模型请求对象
 */
@Data
public class ModelRequest {
    
    /**
     * 提示词/问题
     */
    private String prompt;
    
    /**
     * 平台名称（如：openRouter, siliconFlow 等）
     */
    private String platform;
    
    /**
     * 模型名称（如：gpt-4, deepseek-chat 等）
     */
    private String modelName;
    
    /**
     * 元数据配置（temperature, maxTokens 等）
     */
    private Map<String, Object> meta;
    
    /**
     * 消息列表（用于多轮对话）
     */
    private List<Message> messages;

    public ModelRequest() {
    }

    public ModelRequest(String prompt) {
        this.prompt = prompt;
    }

    public ModelRequest(String prompt, String platform, String modelName) {
        this.prompt = prompt;
        this.platform = platform;
        this.modelName = modelName;
    }

    public ModelRequest(String prompt, Map<String, Object> meta) {
        this.prompt = prompt;
        this.meta = meta;
    }

    public ModelRequest(String prompt, String platform, String modelName, Map<String, Object> meta) {
        this.prompt = prompt;
        this.platform = platform;
        this.modelName = modelName;
        this.meta = meta;
    }

    public ModelRequest(String prompt, String platform, String modelName, Map<String, Object> meta, List<Message> messages) {
        this.prompt = prompt;
        this.platform = platform;
        this.modelName = modelName;
        this.meta = meta;
        this.messages = messages;
    }
}
