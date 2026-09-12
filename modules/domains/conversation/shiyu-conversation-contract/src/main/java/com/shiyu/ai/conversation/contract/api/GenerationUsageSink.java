package com.shiyu.ai.conversation.contract.api;

import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

/**
 * GenerationUsageSink 接口，定义会话模块的能力边界。
 */
public interface GenerationUsageSink {
    /**
     * 处理completed。
     *
     * @param run 运行记录。
     */
    default void completed(GenerationRun run) {}

    /**
     * 处理completed。
     *
     * @param run 运行记录。
     * @param tenantId 租户标识。
     * @param ownerUserId 所有者用户标识。
     */
    default void completed(GenerationRun run, TenantId tenantId, UserId ownerUserId) {
        completed(run);
    }

    /**
     * 处理failed。
     *
     * @param run 运行记录。
     */
    default void failed(GenerationRun run) {}
}
