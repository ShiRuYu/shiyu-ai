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

/** 管理内存中的一次性浏览器授权凭据和非持久化控制台会话。 */
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

    /** 创建 URL 片段凭据，服务端只保留其摘要。 */
    public String issueOneTimeCode() {
        byte[] secret = new byte[SECRET_BYTES];
        secureRandom.nextBytes(secret);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
        oneTimeCodeDigests.put(digest(code), clock.instant().plus(GRANT_LIFETIME));
        return code;
    }

    /** 原子消费有效授权凭据并创建内存中的浏览器会话。 */
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

    /** 保存控制台浏览器会话的标识、CSRF 令牌和签发时间。 */
    public record Session(String sessionId, String csrfToken, Instant issuedAt) {}
}
