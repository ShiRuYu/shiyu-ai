package com.shiyu.ai.web.conversation;

import com.shiyu.ai.chat.product.ConversationExchangeCodec;
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
 * Short-lived, tenant-bound import confirmation state.  The preview token is
 * intentionally single-use and contains no imported content; the content is
 * re-hashed at confirmation time to prevent swapping the reviewed payload.
 */
@Component
public class ConversationImportPreviewStore {
    private static final Duration TTL = Duration.ofMinutes(10);
    private final Map<String, Pending> pending = new ConcurrentHashMap<>();

    public Preview issue(long tenantId, long ownerUserId, String format, String content,
                         List<ConversationExchangeCodec.ImportedMessage> messages) {
        cleanup();
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(TTL);
        pending.put(token, new Pending(tenantId, ownerUserId, normalize(format), digest(content), expiresAt, messages));
        return new Preview(token, expiresAt, messages);
    }

    public List<ConversationExchangeCodec.ImportedMessage> consume(long tenantId, long ownerUserId,
                                                                     String token, String format, String content) {
        if (token == null || token.isBlank()) throw new IllegalArgumentException("previewToken is required");
        Pending value = pending.remove(token);
        if (value == null || value.expiresAt().isBefore(Instant.now())
                || value.tenantId() != tenantId || value.ownerUserId() != ownerUserId
                || !value.format().equals(normalize(format)) || !value.digest().equals(digest(content))) {
            throw new IllegalArgumentException("import preview is missing, expired, or does not match the payload");
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
            byte[] hash = MessageDigest.getInstance("SHA-256").digest((content == null ? "" : content).getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private record Pending(long tenantId, long ownerUserId, String format, String digest,
                           Instant expiresAt, List<ConversationExchangeCodec.ImportedMessage> messages) { }
    public record Preview(String token, Instant expiresAt,
                          List<ConversationExchangeCodec.ImportedMessage> messages) { }
}
