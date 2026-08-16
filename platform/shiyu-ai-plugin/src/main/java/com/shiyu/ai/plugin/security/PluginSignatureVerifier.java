package com.shiyu.ai.plugin.security;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.security.MessageDigest;

/** Ed25519 verification for installable plugins. Unsigned plugins are development-only. */
public final class PluginSignatureVerifier {
    private PluginSignatureVerifier() {}
    public static boolean verify(String manifest, String signatureBase64, String publicKeyBase64) {
        try {
            PublicKey key = KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBase64)));
            Signature verifier = Signature.getInstance("Ed25519");
            verifier.initVerify(key);
            verifier.update(manifest.getBytes(StandardCharsets.UTF_8));
            return verifier.verify(Base64.getDecoder().decode(signatureBase64));
        } catch (Exception ex) { return false; }
    }
    public static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception ex) { throw new IllegalStateException("SHA-256 unavailable", ex); }
    }

    /** Stable publisher identity used by the trusted-publisher allow-list. */
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
