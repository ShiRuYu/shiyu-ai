package com.shiyu.ai.plugin.market;

import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Safe fallback for standalone plugin tests; production wiring uses the DAL store. */
@Component
@ConditionalOnMissingBean(PluginMarketStore.class)
public class InMemoryPluginMarketStore implements PluginMarketStore {
    private final ConcurrentHashMap<String, PluginMarketEntry> entries = new ConcurrentHashMap<>();
    @Override public PluginMarketEntry save(PluginMarketEntry entry) {
        String key = key(entry.id(), entry.version());
        if (entries.putIfAbsent(key, entry) != null) throw new IllegalStateException("plugin version already exists");
        return entry;
    }
    @Override public List<PluginMarketEntry> list() { return entries.values().stream().sorted(java.util.Comparator.comparing(PluginMarketEntry::id)).toList(); }
    @Override public Optional<PluginMarketEntry> find(String id) { return entries.values().stream().filter(entry -> entry.id().equals(id)).max(java.util.Comparator.comparing(PluginMarketEntry::publishedAt)); }
    @Override public void disable(String id) { entries.replaceAll((key, value) -> value.id().equals(id) ? new PluginMarketEntry(value.id(), value.version(), value.source(), value.manifest(), value.signature(), value.publisherKey(), value.permissions(), value.checksum(), value.updatePolicy(), value.publishedAt(), false) : value); }
    private static String key(String id, String version) { return id + "\u0000" + version; }
}
