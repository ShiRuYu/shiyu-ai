package com.shiyu.ai.memory.request;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * 保存消息请求参数
 */
public class SaveMessageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String sessionId;
    private Long userId;
    private String agentId;
    private String role;
    private String content;

    public SaveMessageRequest() {}

    public SaveMessageRequest(String sessionId, Long userId, String agentId, String role, String content) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId must not be null");
        this.userId = userId;
        this.agentId = agentId;
        this.role = Objects.requireNonNull(role, "role must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String sessionId;
        private Long userId;
        private String agentId;
        private String role;
        private String content;
        public Builder sessionId(String v) { this.sessionId = v; return this; }
        public Builder userId(Long v) { this.userId = v; return this; }
        public Builder agentId(String v) { this.agentId = v; return this; }
        public Builder role(String v) { this.role = v; return this; }
        public Builder content(String v) { this.content = v; return this; }
        public SaveMessageRequest build() {
            return new SaveMessageRequest(sessionId, userId, agentId, role, content);
        }
    }
}
