package com.shiyu.ai.tooling.implementation.plugin.security;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 校验插件包签名并阻止未信任的发布者。
 */
public final class PluginSignatureVerifier {
    private PluginSignatureVerifier() {}

    /**
     * {@code verify} 执行当前类型定义的业务操作。
     *
     * @param manifest 参数值，用于执行当前操作。
     * @param signatureBase64 参数值，用于执行当前操作。
     * @param publicKeyBase64 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static boolean verify(String manifest, String signatureBase64, String publicKeyBase64) {
        try {
            PublicKey key =
                    KeyFactory.getInstance("Ed25519")
                            .generatePublic(
                                    new X509EncodedKeySpec(
                                            Base64.getDecoder().decode(publicKeyBase64)));
            Signature verifier = Signature.getInstance("Ed25519");
            verifier.initVerify(key);
            verifier.update(manifest.getBytes(StandardCharsets.UTF_8));
            return verifier.verify(Base64.getDecoder().decode(signatureBase64));
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * {@code sha256} 执行当前类型定义的业务操作。
     *
     * @param value 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static String sha256(String value) {
        try {
            byte[] digest =
                    MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    /**
     * 计算插件指纹。
     *
     * @param publicKeyBase64 publicKeyBase64 参数。
     *
     * @return 处理结果。
     */
    public static String fingerprint(String publicKeyBase64) {
        try {
            byte[] key = Base64.getDecoder().decode(publicKeyBase64);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key);
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}
