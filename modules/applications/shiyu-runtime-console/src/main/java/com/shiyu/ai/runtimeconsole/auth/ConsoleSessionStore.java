package com.shiyu.ai.runtimeconsole.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory one-time browser grants and non-persistent console sessions. */
public final class ConsoleSessionStore {

    private static final Duration GRANT_LIFETIME = Duration.ofMinutes(2);
    private static final int SECRET_BYTES = 32;

    private final Clock clock;
    private final SecureRandom secureRandom;
    private final Map<String, Instant> oneTimeCodeDigests = new ConcurrentHashMap<>();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public ConsoleSessionStore(Clock clock) {
        this(clock, new SecureRandom());
    }

    ConsoleSessionStore(Clock clock, SecureRandom secureRandom) {
        this.clock = clock;
        this.secureRandom = secureRandom;
    }

    /** Creates a URL-fragment credential. Only its digest is retained by the server. */
    public String issueOneTimeCode() {
        byte[] secret = new byte[SECRET_BYTES];
        secureRandom.nextBytes(secret);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
        oneTimeCodeDigests.put(digest(code), clock.instant().plus(GRANT_LIFETIME));
        return code;
    }

    /** Atomically consumes a valid grant and creates an in-memory browser session. */
    public Session exchange(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String codeDigest = digest(code);
        Instant expiresAt = oneTimeCodeDigests.get(codeDigest);
        if (expiresAt == null) {
            return null;
        }
        if (!expiresAt.isAfter(clock.instant())) {
            oneTimeCodeDigests.remove(codeDigest, expiresAt);
            return null;
        }
        if (!oneTimeCodeDigests.remove(codeDigest, expiresAt)) {
            return null;
        }

        Session session = new Session(randomSecret(), randomSecret(), clock.instant());
        sessions.put(session.sessionId(), session);
        return session;
    }

    public Session findSession(String sessionId) {
        return sessionId == null || sessionId.isBlank() ? null : sessions.get(sessionId);
    }

    public boolean revoke(String sessionId) {
        return sessionId != null && sessions.remove(sessionId) != null;
    }

    private String randomSecret() {
        byte[] secret = new byte[SECRET_BYTES];
        secureRandom.nextBytes(secret);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
    }

    private static String digest(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    public record Session(String sessionId, String csrfToken, Instant issuedAt) {}
}
