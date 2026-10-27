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
     * 续期仍在运行的生成配额预留。
     *
     * @param actor 调用方上下文。
     * @param run 运行记录。
     * @return 续期后预留是否仍有效
     */
    default boolean renew(ActorContext actor, GenerationRun run) {
        return true;
    }

    /** 返回配额续期回调的间隔，单位为毫秒。 */
    default long renewalIntervalMillis() {
        return 60_000L;
    }

    /**
     * 按生成运行的实际用量结算其配额预留。
     *
     * @param actor 发起生成的操作主体及租户上下文
     * @param run 携带实际用量的生成运行记录
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
