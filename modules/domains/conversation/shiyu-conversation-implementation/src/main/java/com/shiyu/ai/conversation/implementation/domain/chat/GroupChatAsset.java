package com.shiyu.ai.conversation.implementation.domain.chat;

import java.time.Instant;

/**
 * {@code GroupChatAsset} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param ownerUserId 所属用户标识，表示该记录组件承载的数据。
 * @param group group 属性，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 * @param updatedAt 更新时间，表示该记录组件承载的数据。
 */
public record GroupChatAsset(
        String id,
        long tenantId,
        long ownerUserId,
        GroupChat group,
        Instant createdAt,
        Instant updatedAt) {}
