package com.shiyu.ai.conversation.implementation.web.support.imports;

import com.shiyu.ai.conversation.implementation.domain.chat.ConversationExchangeCodec;
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
 * 保存会话导入预览结果并按预览标识读取。
 */
@Component
public class ConversationImportPreviewStore {
    private static final Duration TTL = Duration.ofMinutes(10);
    private final Map<String, Pending> pending = new ConcurrentHashMap<>();

    /**
     * {@code issue} 校验当前操作的输入或状态是否满足约束。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param format 参数值，用于执行当前操作。
     * @param content 参数值，用于执行当前操作。
     * @param messages 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code consume} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param token 参数值，用于执行当前操作。
     * @param format 参数值，用于执行当前操作。
     * @param content 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code Pending} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param tenantId 租户标识，表示该记录组件承载的数据。
     * @param ownerUserId 所属用户标识，表示该记录组件承载的数据。
     * @param format format 属性，表示该记录组件承载的数据。
     * @param digest digest 属性，表示该记录组件承载的数据。
     * @param expiresAt 过期时间，表示该记录组件承载的数据。
     * @param messages 消息列表，表示该记录组件承载的数据。
     */
    private record Pending(
            TenantId tenantId,
            long ownerUserId,
            String format,
            String digest,
            Instant expiresAt,
            List<ConversationExchangeCodec.ImportedMessage> messages) {}

    /**
     * {@code Preview} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param token 令牌，表示该记录组件承载的数据。
     * @param expiresAt 过期时间，表示该记录组件承载的数据。
     * @param messages 消息列表，表示该记录组件承载的数据。
     */
    public record Preview(
            String token,
            Instant expiresAt,
            List<ConversationExchangeCodec.ImportedMessage> messages) {}
}
