package com.shiyu.ai.plugin.market;

import java.util.List;
import java.util.Optional;

/** Persistence boundary for the plugin catalog; H2 is the default platform implementation. */
public interface PluginMarketStore {
    PluginMarketEntry save(PluginMarketEntry entry);
    List<PluginMarketEntry> list();
    Optional<PluginMarketEntry> find(String id);
    void disable(String id);
}
