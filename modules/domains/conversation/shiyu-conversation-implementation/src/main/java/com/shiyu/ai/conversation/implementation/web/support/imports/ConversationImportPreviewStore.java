package com.shiyu.ai.conversation.implementation.web.support.imports;

import com.shiyu.ai.conversation.implementation.domain.chat.codec.ConversationExchangeCodec;
import com.shiyu.ai.kernel.context.TenantId;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理 会话 Import Preview 相关的运行时状态、注册信息或临时数据。
 */
@Component
public class ConversationImportPreviewStore {
    private static final Duration TTL = Duration.ofMinutes(10);
    private final Map<String, Pending> pending = new ConcurrentHashMap<>();

    /**
     * 校验或判断 会话 Import Preview 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param format 用于完成本次业务处理的 format 参数。
     * @param content 用于完成本次业务处理的 content 参数。
     * @param messages 用于完成本次业务处理的 messages 参数。
     * @return 返回 会话 Import Preview 相关操作生成的结果数据。
     */
    public Preview issue(
            TenantId tenantId,
            long ownerUserId,
            String format,
            String content,
            List<ConversationExchangeCodec.ImportedMessage> messages) {
        java.util.Objects.requireNonNull(tenantId, "tenantId");
        cleanup();
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(TTL);
        pending.put(
                token,
                new Pending(
                        tenantId,
                        ownerUserId,
                        normalize(format),
                        digest(content),
                        expiresAt,
                        messages));
        return new Preview(token, expiresAt, messages);
    }

    /**
     * 处理 会话 Import Preview 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param token 用于完成本次业务处理的 token 参数。
     * @param format 用于完成本次业务处理的 format 参数。
     * @param content 用于完成本次业务处理的 content 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<ConversationExchangeCodec.ImportedMessage> consume(
            TenantId tenantId, long ownerUserId, String token, String format, String content) {
        java.util.Objects.requireNonNull(tenantId, "tenantId");
        if (token == null || token.isBlank())
            throw new IllegalArgumentException("previewToken is required");
        Pending value = pending.remove(token);
        if (value == null
                || value.expiresAt().isBefore(Instant.now())
                || !value.tenantId().equals(tenantId)
                || value.ownerUserId() != ownerUserId
                || !value.format().equals(normalize(format))
                || !value.digest().equals(digest(content))) {
            throw new IllegalArgumentException(
                    "import preview is missing, expired, or does not match the payload");
        }
        return value.messages();
    }

    private void cleanup() {
        Instant now = Instant.now();
        pending.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private static String normalize(String format) {
        String value = format == null ? "jsonl" : format.trim().toLowerCase(java.util.Locale.ROOT);
        return "md".equals(value) ? "markdown" : value;
    }

    private static String digest(String content) {
        try {
            byte[] hash =
                    MessageDigest.getInstance("SHA-256")
                            .digest(
                                    (content == null ? "" : content)
                                            .getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    /**
     * 封装 Pending 相关的不可变数据及其字段约束。
     */
    private record Pending(
            TenantId tenantId,
            long ownerUserId,
            String format,
            String digest,
            Instant expiresAt,
            List<ConversationExchangeCodec.ImportedMessage> messages) {}

    /**
     * 封装 Preview 相关的不可变数据及其字段约束。
     */
    public record Preview(
            String token,
            Instant expiresAt,
            List<ConversationExchangeCodec.ImportedMessage> messages) {}
}
