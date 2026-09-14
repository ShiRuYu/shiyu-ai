package com.shiyu.ai.conversation.implementation.web.support.imports;

import com.shiyu.ai.conversation.implementation.domain.chat.CharacterCardV2;
import com.shiyu.ai.kernel.context.TenantId;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 保存角色卡导入预览结果并按预览标识读取。
 */
@Component
public final class CharacterImportPreviewStore {
    private static final Duration TTL = Duration.ofMinutes(10);
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    /**
     * {@code issue} 校验当前操作的输入或状态是否满足约束。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param userId 参数值，用于执行当前操作。
     * @param payload 参数值，用于执行当前操作。
     * @param filename 参数值，用于执行当前操作。
     * @param card 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public Preview issue(
            TenantId tenantId, long userId, byte[] payload, String filename, CharacterCardV2 card) {
        java.util.Objects.requireNonNull(tenantId, "tenantId");
        String token = UUID.randomUUID().toString();
        Instant expires = Instant.now().plus(TTL);
        entries.put(token, new Entry(tenantId, userId, digest(payload), filename, card, expires));
        return new Preview(token, expires, filename, card);
    }

    /**
     * {@code consume} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param userId 参数值，用于执行当前操作。
     * @param token 参数值，用于执行当前操作。
     * @param payload 参数值，用于执行当前操作。
     * @param filename 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public CharacterCardV2 consume(
            TenantId tenantId, long userId, String token, byte[] payload, String filename) {
        java.util.Objects.requireNonNull(tenantId, "tenantId");
        if (token == null || token.isBlank())
            throw new IllegalArgumentException("previewToken is required");
        Entry entry = entries.remove(token);
        if (entry == null
                || entry.expires().isBefore(Instant.now())
                || !entry.tenantId().equals(tenantId)
                || entry.userId() != userId
                || !entry.digest().equals(digest(payload))
                || !java.util.Objects.equals(entry.filename(), filename)) {
            throw new IllegalArgumentException("character import preview is invalid or expired");
        }
        return entry.card();
    }

    private String digest(byte[] payload) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(payload == null ? new byte[0] : payload));
        } catch (Exception e) {
            throw new IllegalStateException("digest unavailable", e);
        }
    }

    /**
     * {@code Entry} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param tenantId 租户标识，表示该记录组件承载的数据。
     * @param userId 用户标识，表示该记录组件承载的数据。
     * @param digest digest 属性，表示该记录组件承载的数据。
     * @param filename 文件名，表示该记录组件承载的数据。
     * @param card card 属性，表示该记录组件承载的数据。
     * @param expires expires 属性，表示该记录组件承载的数据。
     */
    private record Entry(
            TenantId tenantId,
            long userId,
            String digest,
            String filename,
            CharacterCardV2 card,
            Instant expires) {}

    /**
     * {@code Preview} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param token 令牌，表示该记录组件承载的数据。
     * @param expiresAt 过期时间，表示该记录组件承载的数据。
     * @param filename 文件名，表示该记录组件承载的数据。
     * @param card card 属性，表示该记录组件承载的数据。
     */
    public record Preview(String token, Instant expiresAt, String filename, CharacterCardV2 card) {}
}
