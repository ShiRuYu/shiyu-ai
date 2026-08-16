package com.shiyu.ai.plugin.market;

import com.shiyu.ai.plugin.security.PluginSignatureVerifier;
import com.shiyu.ai.plugin.security.TrustedPublisherRegistry;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;

/** Local catalog implementation for P0-P2; a repository-backed implementation can replace it in P3. */
@Service
public class PluginMarketService {
    private final PluginMarketStore store;
    private final TrustedPublisherRegistry publishers;
    private final boolean developmentMode;
    public PluginMarketService(ObjectProvider<PluginMarketStore> stores,
                               @Value("${shiyu.plugins.development-mode:false}") boolean developmentMode) {
        this.store = stores.getIfAvailable(InMemoryPluginMarketStore::new);
        this.publishers = new TrustedPublisherRegistry();
        this.developmentMode = developmentMode;
    }
    public PluginMarketEntry publish(PluginMarketEntry entry, boolean developmentMode) {
        boolean allowUnsigned = this.developmentMode && developmentMode;
        boolean signed = PluginSignatureVerifier.verify(entry.manifest(), entry.signature(), entry.publisherKey());
        if (!signed && !allowUnsigned) throw new SecurityException("unsigned or invalid plugin manifest");
        if (signed && !allowUnsigned && !publishers.isTrusted(entry.publisherKey())) {
            throw new SecurityException("plugin publisher is not trusted");
        }
        String checksum = PluginSignatureVerifier.sha256(entry.manifest());
        if (entry.checksum() != null && !entry.checksum().isBlank() && !checksum.equalsIgnoreCase(entry.checksum())) {
            throw new SecurityException("plugin manifest checksum mismatch");
        }
        PluginMarketEntry stored = new PluginMarketEntry(entry.id(), entry.version(), entry.source(), entry.manifest(), entry.signature(), entry.publisherKey(), entry.permissions(), checksum, entry.updatePolicy(), Instant.now(), true);
        return store.save(stored);
    }
    public List<PluginMarketEntry> list() { return store.list(); }
    public PluginMarketEntry find(String id) { return store.find(id).orElse(null); }
    public void disable(String id) { store.disable(id); }
}
