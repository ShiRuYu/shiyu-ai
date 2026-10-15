package com.shiyu.ai.model.implementation.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

/** 验证平台凭证在数据库中的加密、租户绑定和失败关闭行为。 */
class PlatformCredentialCipherTest {
    private static final String MASTER_KEY = Base64.getEncoder()
            .encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    @Test
    void encryptsWithRandomNonceAndBindsCiphertextToTenant() {
        PlatformCredentialCipher cipher = new PlatformCredentialCipher(MASTER_KEY);

        String first = cipher.protect(7L, "temporary-test-secret");
        String second = cipher.protect(7L, "temporary-test-secret");

        assertThat(first).startsWith("enc:v1:").doesNotContain("temporary-test-secret");
        assertThat(second).isNotEqualTo(first);
        assertThat(cipher.unprotect(7L, first)).isEqualTo("temporary-test-secret");
        assertThatThrownBy(() -> cipher.unprotect(8L, first))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void refusesNonblankCredentialsWithoutAConfiguredMasterKey() {
        PlatformCredentialCipher cipher = new PlatformCredentialCipher("");

        assertThat(cipher.protect(7L, "")).isEmpty();
        assertThatThrownBy(() -> cipher.protect(7L, "temporary-test-secret"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> cipher.unprotect(7L, "legacy-plaintext"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void detectsTamperedCiphertextAndOversizedCredentials() {
        PlatformCredentialCipher cipher = new PlatformCredentialCipher(MASTER_KEY);
        String stored = cipher.protect(7L, "temporary-test-secret");
        int payloadStart = "enc:v1:".length();
        char replacement = stored.charAt(payloadStart) == 'A' ? 'B' : 'A';
        String tampered = stored.substring(0, payloadStart)
                + replacement
                + stored.substring(payloadStart + 1);

        assertThatThrownBy(() -> cipher.unprotect(7L, tampered))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> cipher.protect(7L, "x".repeat(400)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsUnknownCipherVersionAndMalformedDataInsteadOfTreatingThemAsPlaintext() {
        PlatformCredentialCipher cipher = new PlatformCredentialCipher(MASTER_KEY);

        assertThatThrownBy(() -> cipher.unprotect(7L, "enc:v2:unknown"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> cipher.unprotect(7L, "enc:v1:AA"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> cipher.unprotect(7L, "enc:v1:***"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsInvalidMasterKeyAndTenantId() {
        assertThatThrownBy(() -> new PlatformCredentialCipher("not-base64"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new PlatformCredentialCipher(
                Base64.getEncoder().encodeToString(new byte[16])))
                .isInstanceOf(IllegalStateException.class);
        PlatformCredentialCipher cipher = new PlatformCredentialCipher(MASTER_KEY);
        assertThat(cipher.unprotect(7L, null)).isEmpty();
        assertThat(cipher.unprotect(7L, "")).isEmpty();
        assertThat(cipher.isProtected(null)).isFalse();
        assertThatThrownBy(() -> cipher.protect(0L, "temporary-test-secret"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
