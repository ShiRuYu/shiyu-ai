package com.shiyu.ai.conversation.implementation.domain.chat.model;

/**
 * 定义 Speaker 可用的枚举值及其业务语义。
 */
public enum SpeakerPolicy {
    MANUAL,
    ROUND_ROBIN,
    MODEL_ROUTED
}
