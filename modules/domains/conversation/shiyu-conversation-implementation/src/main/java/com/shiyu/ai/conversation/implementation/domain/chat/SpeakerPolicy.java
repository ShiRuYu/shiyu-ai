package com.shiyu.ai.conversation.implementation.domain.chat;

/**
 * {@code SpeakerPolicy} 表示会话模块中的一组受控业务状态或分类。
 */
public enum SpeakerPolicy {
    MANUAL,
    ROUND_ROBIN,
    MODEL_ROUTED
}
