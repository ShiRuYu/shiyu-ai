package com.shiyu.ai.conversation.web;

import com.shiyu.ai.conversation.chat.CharacterCardV2;
import com.shiyu.ai.kernel.context.TenantId;

import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Short-lived tenant/user-bound confirmation token for character imports. */
@Component
public final class CharacterImportPreviewStore {
    private static final Duration TTL = Duration.ofMinutes(10);
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    public Preview issue(TenantId tenantId, long userId, byte[] payload, String filename, CharacterCardV2 card) {
        java.util.Objects.requireNonNull(tenantId, "tenantId");
        String token = UUID.randomUUID().toString();
        Instant expires = Instant.now().plus(TTL);
        entries.put(token, new Entry(tenantId, userId, digest(payload), filename, card, expires));
        return new Preview(token, expires, filename, card);
    }

    public CharacterCardV2 consume(TenantId tenantId, long userId, String token, byte[] payload, String filename) {
        java.util.Objects.requireNonNull(tenantId, "tenantId");
        if (token == null || token.isBlank()) throw new IllegalArgumentException("previewToken is required");
        Entry entry = entries.remove(token);
        if (entry == null || entry.expires().isBefore(Instant.now()) || !entry.tenantId().equals(tenantId) || entry.userId() != userId
                || !entry.digest().equals(digest(payload)) || !java.util.Objects.equals(entry.filename(), filename)) {
            throw new IllegalArgumentException("character import preview is invalid or expired");
        }
        return entry.card();
    }

    private String digest(byte[] payload) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(payload == null ? new byte[0] : payload)); }
        catch (Exception e) { throw new IllegalStateException("digest unavailable", e); }
    }

    private record Entry(TenantId tenantId, long userId, String digest, String filename, CharacterCardV2 card, Instant expires) { }
    public record Preview(String token, Instant expiresAt, String filename, CharacterCardV2 card) { }
}
