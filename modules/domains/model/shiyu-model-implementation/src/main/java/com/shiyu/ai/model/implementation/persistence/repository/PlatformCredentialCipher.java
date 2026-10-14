package com.shiyu.ai.model.implementation.persistence.repository;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 使用部署环境提供的主密钥保护平台凭证，并把密文绑定到所属租户。 */
@Component
public final class PlatformCredentialCipher {
    private static final String PREFIX = "enc:v1:";
    private static final int NONCE_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final int MAX_SECRET_BYTES = 340;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SecretKeySpec key;

    /**
     * 创建平台凭证加密器。
     *
     * @param encodedKey Base64 编码的 256 位主密钥；空值只允许无凭证平台运行。
     */
    public PlatformCredentialCipher(
            @Value("${shiyu.ai.platform-key-encryption-key:}") String encodedKey) {
        if (encodedKey == null || encodedKey.isBlank()) {
            this.key = null;
            return;
        }
        try {
            byte[] raw = Base64.getDecoder().decode(encodedKey);
            if (raw.length != 32) {
                throw new IllegalArgumentException("master key must contain 32 bytes");
            }
            this.key = new SecretKeySpec(raw, "AES");
            Arrays.fill(raw, (byte) 0);
        } catch (IllegalArgumentException failure) {
            throw new IllegalStateException("Invalid platform credential encryption key", failure);
        }
    }

    /**
     * 加密待写入数据库的凭证。
     *
     * @param tenantId 凭证所属租户。
     * @param plaintext 明文凭证。
     * @return 可持久化的密文；空凭证保持为空。
     */
    public String protect(long tenantId, String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) return "";
        requireKey();
        byte[] input = plaintext.getBytes(StandardCharsets.UTF_8);
        if (input.length > MAX_SECRET_BYTES) {
            throw new IllegalArgumentException("Platform credential is too long");
        }
        byte[] nonce = new byte[NONCE_BYTES];
        RANDOM.nextBytes(nonce);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, nonce));
            cipher.updateAAD(aad(tenantId));
            byte[] encrypted = cipher.doFinal(input);
            byte[] combined = ByteBuffer.allocate(nonce.length + encrypted.length)
                    .put(nonce).put(encrypted).array();
            return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(combined);
        } catch (GeneralSecurityException failure) {
            throw new IllegalStateException("Platform credential encryption failed", failure);
        } finally {
            Arrays.fill(input, (byte) 0);
        }
    }

    /**
     * 解密数据库凭证。旧明文仅在有主密钥时供迁移流程读取。
     *
     * @param tenantId 凭证所属租户。
     * @param stored 数据库原值。
     * @return 明文凭证。
     */
    public String unprotect(long tenantId, String stored) {
        if (stored == null || stored.isEmpty()) return "";
        requireKey();
        if (stored.startsWith("enc:") && !isProtected(stored)) {
            throw new IllegalStateException("Unsupported platform credential ciphertext version");
        }
        if (!isProtected(stored)) return stored;
        try {
            byte[] combined = Base64.getUrlDecoder().decode(stored.substring(PREFIX.length()));
            if (combined.length < NONCE_BYTES + TAG_BITS / 8) {
                throw new IllegalStateException("Invalid platform credential ciphertext");
            }
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key,
                    new GCMParameterSpec(TAG_BITS, combined, 0, NONCE_BYTES));
            cipher.updateAAD(aad(tenantId));
            byte[] plaintext = cipher.doFinal(
                    combined, NONCE_BYTES, combined.length - NONCE_BYTES);
            try {
                return new String(plaintext, StandardCharsets.UTF_8);
            } finally {
                Arrays.fill(plaintext, (byte) 0);
            }
        } catch (GeneralSecurityException | IllegalArgumentException failure) {
            throw new IllegalStateException("Platform credential decryption failed", failure);
        }
    }

    /**
     * 判断数据库原值是否为当前版本密文。
     *
     * @param stored 数据库原值。
     * @return 当前版本密文时为真。
     */
    public boolean isProtected(String stored) {
        return stored != null && stored.startsWith(PREFIX);
    }

    private void requireKey() {
        if (key == null) {
            throw new IllegalStateException("Platform credential encryption key is not configured");
        }
    }

    private static byte[] aad(long tenantId) {
        if (tenantId <= 0) throw new IllegalArgumentException("tenantId must be positive");
        return ByteBuffer.allocate(Long.BYTES + 21)
                .putLong(tenantId)
                .put("model_ai_platform_key".getBytes(StandardCharsets.US_ASCII))
                .array();
    }
}
