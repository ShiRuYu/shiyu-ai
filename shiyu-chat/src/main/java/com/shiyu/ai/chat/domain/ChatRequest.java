package com.shiyu.ai.chat.domain;

/**
 * 对话请求记录
 */
public record ChatRequest(
    String query,
    String sessionId,
    String userId,
    String platform,
    String modelName
) {
    public ChatRequest {
        // 默认值处理
        if (query == null || query.trim().isEmpty()) {
            query = "你能帮我什么？";
        }
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = null; // 由 Controller 生成
        }
        if (userId == null || userId.trim().isEmpty()) {
            userId = "anonymous";
        }
        if (platform == null || platform.trim().isEmpty()) {
            platform = "SILICON_FLOW";
        }
    }
}
