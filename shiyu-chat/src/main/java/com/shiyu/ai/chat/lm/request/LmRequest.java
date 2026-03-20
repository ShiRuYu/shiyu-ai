package com.shiyu.ai.chat.lm.request;

import lombok.Data;
import org.springframework.ai.chat.messages.Message;

import java.util.List;
import java.util.Map;

/**
 * 模型请求对象
 */
@Data
public class LmRequest {
    
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
    
    /**
     * 调用来源（如：CHAT_DIRECT、CHAT_COT、CHAT_TOT、IntentService 等）
     * 用于追踪模型调用的业务场景，便于日志分析和监控
     */
    private String source;

    public LmRequest(String prompt, String platform, String modelName, String source) {
        this.prompt = prompt;
        this.platform = platform;
        this.modelName = modelName;
        this.source = source;
    }
    
    public LmRequest(String prompt, String platform, String modelName, Map<String, Object> meta, List<Message> messages, String source) {
        this.prompt = prompt;
        this.platform = platform;
        this.modelName = modelName;
        this.meta = meta;
        this.messages = messages;
        this.source = source;
    }
}
