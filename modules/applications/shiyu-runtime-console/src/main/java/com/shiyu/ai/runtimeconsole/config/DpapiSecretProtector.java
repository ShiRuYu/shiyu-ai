package com.shiyu.ai.runtimeconsole.config;

import com.sun.jna.platform.win32.Crypt32Util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

/** 使用 Windows 当前用户 DPAPI 保护密钥，故意不提供明文回退。 */
public final class DpapiSecretProtector implements ConfigSecretProtector {

    private static final String PREFIX = "dpapi-current-user:";

    @Override
    public String protect(String plaintext) {
        requireWindows();
        if (plaintext == null) {
            throw new IllegalArgumentException("Secret value must not be null");
        }
        try {
            byte[] protectedBytes = Crypt32Util.cryptProtectData(plaintext.getBytes(StandardCharsets.UTF_8));
            return PREFIX + Base64.getEncoder().encodeToString(protectedBytes);
        } catch (RuntimeException exception) {
            throw new SecretProtectionException("Windows DPAPI could not protect the configuration secret", exception);
        }
    }

    @Override
    public String unprotect(String ciphertext) {
        requireWindows();
        if (ciphertext == null || !ciphertext.startsWith(PREFIX)) {
            throw new SecretProtectionException("Configuration secret is not a DPAPI CurrentUser value");
        }
        try {
            byte[] protectedBytes = Base64.getDecoder().decode(ciphertext.substring(PREFIX.length()));
            byte[] plaintext = Crypt32Util.cryptUnprotectData(protectedBytes);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (RuntimeException exception) {
            throw new SecretProtectionException(
                    "Windows DPAPI could not decrypt this secret for the current Windows user", exception);
        }
    }

    private static void requireWindows() {
        if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows")) {
            throw new SecretProtectionException("Protected startup secrets require Windows DPAPI; plaintext fallback is disabled");
        }
    }

    /** 表示配置密钥加解密失败。 */
    public static final class SecretProtectionException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public SecretProtectionException(String message) {
            super(message);
        }

        public SecretProtectionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
