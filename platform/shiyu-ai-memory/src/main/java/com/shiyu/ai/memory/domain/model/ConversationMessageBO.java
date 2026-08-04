package com.shiyu.ai.memory.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话消息业务对象
 */
@Data
public class ConversationMessageBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息 ID
     */
    private Long id;

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * Agent ID
     */
    private String agentId;

    /**
     * 角色（user/assistant/system）
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;

}
