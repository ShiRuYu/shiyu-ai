package com.shiyu.ai.plugin.security;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

class PluginSignatureVerifierTest {
    @Test void verifiesEd25519AndDetectsTampering() throws Exception {
        var pair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        String manifest = "{\"id\":\"demo\",\"version\":\"1\"}";
        var signer = java.security.Signature.getInstance("Ed25519");
        signer.initSign(pair.getPrivate());
        signer.update(manifest.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(signer.sign());
        String key = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
        assertTrue(PluginSignatureVerifier.verify(manifest, signature, key));
        assertFalse(PluginSignatureVerifier.verify(manifest + "!", signature, key));
        assertEquals(64, PluginSignatureVerifier.sha256(manifest).length());
        assertEquals(64, PluginSignatureVerifier.fingerprint(key).length());
    }

    @Test void trustedPublisherMatchesFingerprintOrExactKey() throws Exception {
        var pair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        String key = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
        assertTrue(new TrustedPublisherRegistry(PluginSignatureVerifier.fingerprint(key)).isTrusted(key));
        assertTrue(new TrustedPublisherRegistry(key).isTrusted(key));
        assertFalse(new TrustedPublisherRegistry("").isTrusted(key));
    }
}
