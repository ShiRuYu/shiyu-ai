package com.shiyu.ai.iam.contract;

import com.shiyu.ai.kernel.context.ActorContext;

/**
 * 定义 平台 用量 Access 相关的协作契约和调用边界。
 */
public interface PlatformUsageAccess {

    /**
     * 判断当前 Actor 是否满足平台用量入口的身份边界。
     *
     * @param actor 当前认证 Actor。
     * @return 满足默认租户超级管理员边界时返回 true。
     */
    boolean canReadPlatformUsage(ActorContext actor);
}
