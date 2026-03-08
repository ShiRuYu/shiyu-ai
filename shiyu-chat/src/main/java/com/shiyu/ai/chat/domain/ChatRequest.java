package com.shiyu.ai.chat.domain;

/**
 * 对话请求记录
 */
public record ChatRequest(
    String text,
    String sessionId,
    String userId
) {
    public ChatRequest {
        // 默认值处理
        if (text == null || text.trim().isEmpty()) {
            text = "你能帮我什么？";
        }
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = null; // 由 Controller 生成
        }
        if (userId == null || userId.trim().isEmpty()) {
            userId = "anonymous";
        }
    }
}
