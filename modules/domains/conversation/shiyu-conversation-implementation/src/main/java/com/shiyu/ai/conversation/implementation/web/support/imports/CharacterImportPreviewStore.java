package com.shiyu.ai.conversation.implementation.web.support.imports;

import com.shiyu.ai.conversation.implementation.domain.chat.model.CharacterCardV2;
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
 * 管理 Character Import Preview 相关的运行时状态、注册信息或临时数据。
 */
@Component
public final class CharacterImportPreviewStore {
    private static final Duration TTL = Duration.ofMinutes(10);
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    /**
     * 校验或判断 Character Import Preview 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param userId 当前操作涉及的用户标识。
     * @param payload 本次流程携带的事件或业务数据。
     * @param filename 用于完成本次业务处理的 filename 参数。
     * @param card 用于完成本次业务处理的 card 参数。
     * @return 返回 Character Import Preview 相关操作生成的结果数据。
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
     * 处理 Character Import Preview 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param userId 当前操作涉及的用户标识。
     * @param token 用于完成本次业务处理的 token 参数。
     * @param payload 本次流程携带的事件或业务数据。
     * @param filename 用于完成本次业务处理的 filename 参数。
     * @return 返回 Character Import Preview 相关操作生成的结果数据。
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
     * 封装 Entry 相关的不可变数据及其字段约束。
     */
    private record Entry(
            TenantId tenantId,
            long userId,
            String digest,
            String filename,
            CharacterCardV2 card,
            Instant expires) {}

    /**
     * 封装 Preview 相关的不可变数据及其字段约束。
     */
    public record Preview(String token, Instant expiresAt, String filename, CharacterCardV2 card) {}
}
