package com.shiyu.ai.plugin.market;

import com.shiyu.ai.plugin.security.PluginSignatureVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Instant;
import java.util.List;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class PluginMarketServiceTest {
    @Test
    void permitsUnsignedDevelopmentPublishAndPersistsChecksum() {
        PluginMarketStore store = new InMemoryPluginMarketStore();
        ObjectProvider<PluginMarketStore> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable(org.mockito.Mockito.<java.util.function.Supplier<PluginMarketStore>>any())).thenReturn(store);
        PluginMarketService service = new PluginMarketService(provider, true);
        PluginMarketEntry entry = new PluginMarketEntry("demo", "1.0.0", "local", "{\"name\":\"demo\"}", null, null, List.of("read"), Instant.now(), true);

        PluginMarketEntry published = service.publish(entry, true);
        assertEquals(PluginSignatureVerifier.sha256(entry.manifest()), published.checksum());
        assertEquals(published, service.find("demo"));
        assertEquals(1, service.list().size());
        service.disable("demo");
        assertFalse(service.find("demo").enabled());
    }

    @Test
    void rejectsUnsignedProductionAndChecksumMismatch() {
        PluginMarketStore store = new InMemoryPluginMarketStore();
        ObjectProvider<PluginMarketStore> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable(org.mockito.Mockito.<java.util.function.Supplier<PluginMarketStore>>any())).thenReturn(store);
        PluginMarketEntry entry = new PluginMarketEntry("demo", "1.0.0", "local", "manifest", null, null, List.of(), Instant.now(), true);
        assertThrows(SecurityException.class, () -> new PluginMarketService(provider, false).publish(entry, true));

        PluginMarketEntry wrongChecksum = new PluginMarketEntry("demo2", "1.0.0", "local", "manifest", null, null, List.of(), "bad", "MANUAL", Instant.now(), true);
        assertThrows(SecurityException.class, () -> new PluginMarketService(provider, true).publish(wrongChecksum, true));
        assertThrows(IllegalArgumentException.class, () -> new PluginMarketEntry("", "1", "x", "manifest", null, null, List.of(), Instant.now(), true));
    }

    @Test
    void verifiesSignedTrustedPublisherAndNormalizesEntryDefaults() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("Ed25519");
        KeyPair pair = generator.generateKeyPair();
        String manifest = "{\"name\":\"signed\"}";
        java.security.Signature signer = java.security.Signature.getInstance("Ed25519");
        signer.initSign(pair.getPrivate()); signer.update(manifest.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(signer.sign());
        String key = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
        String old = System.getProperty("shiyu.plugin.trusted-publishers");
        System.setProperty("shiyu.plugin.trusted-publishers", key);
        try {
            PluginMarketStore store = new InMemoryPluginMarketStore();
            ObjectProvider<PluginMarketStore> provider = mock(ObjectProvider.class);
            when(provider.getIfAvailable(org.mockito.Mockito.<java.util.function.Supplier<PluginMarketStore>>any())).thenReturn(store);
            PluginMarketService service = new PluginMarketService(provider, false);
            PluginMarketEntry entry = new PluginMarketEntry("signed", "1.0.0", "catalog", manifest, signature, key, null, null, "", Instant.now(), true);
            PluginMarketEntry stored = service.publish(entry, false);
            assertEquals("MANUAL", stored.updatePolicy());
            assertEquals(PluginSignatureVerifier.sha256(manifest), stored.checksum());
            assertThrows(SecurityException.class, () -> service.publish(new PluginMarketEntry("other", "1", "x", manifest, signature, key, List.of(), "bad", "MANUAL", Instant.now(), true), false));
        } finally {
            if (old == null) System.clearProperty("shiyu.plugin.trusted-publishers"); else System.setProperty("shiyu.plugin.trusted-publishers", old);
        }
    }
}
