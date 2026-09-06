package com.shiyu.ai.plugin.spi;

import java.util.Map;

/**
 * 插件描述符
 */
public class PluginDescriptor {

    private final String id;
    private final String name;
    private final String version;
    private final String description;
    private final String author;
    private final String entryClass;
    private final Map<String, Object> config;
    private final long loadedAt;
    private volatile PluginState state;

    public enum PluginState {
        INSTALLED, RESOLVED, STARTING, ACTIVE, STOPPING, STOPPED, FAILED
    }

    public PluginDescriptor(String id, String name, String version,
                            String description, String author,
                            String entryClass, Map<String, Object> config) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.author = author;
        this.entryClass = entryClass;
        this.config = config;
        this.loadedAt = System.currentTimeMillis();
        this.state = PluginState.INSTALLED;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getDescription() { return description; }
    public String getAuthor() { return author; }
    public String getEntryClass() { return entryClass; }
    public Map<String, Object> getConfig() { return config; }
    public long getLoadedAt() { return loadedAt; }
    public PluginState getState() { return state; }
    public void setState(PluginState state) { this.state = state; }
}
