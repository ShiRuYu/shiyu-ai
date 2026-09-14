package com.shiyu.ai.conversation.implementation.domain.chat;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

/**
 * {@code CharacterAsset} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param ownerUserId 所属用户标识，表示该记录组件承载的数据。
 * @param card card 属性，表示该记录组件承载的数据。
 * @param visibility visibility 属性，表示该记录组件承载的数据。
 * @param pngData pngData 属性，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 * @param updatedAt 更新时间，表示该记录组件承载的数据。
 */
public record CharacterAsset(
        String id,
        long tenantId,
        long ownerUserId,
        CharacterCardV2 card,
        String visibility,
        byte[] pngData,
        Instant createdAt,
        Instant updatedAt) {
    public CharacterAsset(
            String id,
            long tenantId,
            long ownerUserId,
            CharacterCardV2 card,
            String visibility,
            Instant createdAt,
            Instant updatedAt) {
        this(id, tenantId, ownerUserId, card, visibility, null, createdAt, updatedAt);
    }

    public CharacterAsset {
        if (card == null) throw new IllegalArgumentException("character card is required");
        visibility =
                visibility == null || visibility.isBlank()
                        ? "PRIVATE"
                        : visibility.toUpperCase(Locale.ROOT);
        if (!List.of("PRIVATE", "PUBLIC", "TENANT").contains(visibility))
            throw new IllegalArgumentException("unsupported character visibility");
        pngData = pngData == null ? null : pngData.clone();
    }

    @Override
    public byte[] pngData() {
        return pngData == null ? null : pngData.clone();
    }
}
