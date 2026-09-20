package com.shiyu.ai.conversation.contract.api;

import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.kernel.context.ActorContext;

/**
 * 校验并控制 生成 相关请求是否允许进入处理流程。
 */
public interface GenerationAdmission {
    /**
     * 处理reserve。
     *
     * @param actor 调用方上下文。
     * @param run 运行记录。
     * @param estimatedPromptTokens estimatedPromptTokens 参数。
     */
    default void reserve(ActorContext actor, GenerationRun run, int estimatedPromptTokens) {}

    /**
     * 设置生成admission。
     *
     * @param actor 调用方上下文。
     * @param run 运行记录。
     */
    default void settle(ActorContext actor, GenerationRun run) {}

    /**
     * 处理release。
     *
     * @param actor 调用方上下文。
     * @param run 运行记录。
     */
    default void release(ActorContext actor, GenerationRun run) {}
}
