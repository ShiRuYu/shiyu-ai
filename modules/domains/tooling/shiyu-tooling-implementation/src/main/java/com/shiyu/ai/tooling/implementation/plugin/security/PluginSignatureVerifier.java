package com.shiyu.ai.tooling.implementation.plugin.security;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 实现 插件 Signature Verifier 相关的业务处理、协作逻辑或基础设施能力。
 */
public final class PluginSignatureVerifier {
    private PluginSignatureVerifier() {}

    /**
     * 执行 插件 Signature Verifier 相关业务数据，并返回处理结果。
     *
     * @param manifest 用于完成本次业务处理的 manifest 参数。
     * @param signatureBase64 用于完成本次业务处理的 signatureBase64 参数。
     * @param publicKeyBase64 用于完成本次业务处理的 publicKeyBase64 参数。
     * @return 返回本次条件判断是否成立。
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
     * 执行 插件 Signature Verifier 相关业务数据，并返回处理结果。
     *
     * @param value 用于完成本次业务处理的 value 参数。
     * @return 返回 插件 Signature Verifier 相关操作生成的结果数据。
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
