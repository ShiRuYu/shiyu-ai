package com.shiyu.ai.tooling.implementation.plugin.market.adapter;
import com.shiyu.ai.tooling.implementation.plugin.market.model.PluginMarketEntry;
import com.shiyu.ai.tooling.implementation.plugin.market.port.PluginMarketStore;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在内存中保存和查询插件市场条目。
 */
@Component
@ConditionalOnMissingBean(PluginMarketStore.class)
public class InMemoryPluginMarketStore implements PluginMarketStore {
    private final ConcurrentHashMap<String, PluginMarketEntry> entries = new ConcurrentHashMap<>();

    /**
     * {@code save} 写入或更新当前模块中的业务数据。
     *
     * @param entry 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public PluginMarketEntry save(PluginMarketEntry entry) {
        String key = key(entry.id(), entry.version());
        if (entries.putIfAbsent(key, entry) != null)
            throw new IllegalStateException("plugin version already exists");
        return entry;
    }

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<PluginMarketEntry> list() {
        return entries.values().stream()
                .sorted(java.util.Comparator.comparing(PluginMarketEntry::id))
                .toList();
    }

    /**
     * {@code find} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<PluginMarketEntry> find(String id) {
        return entries.values().stream()
                .filter(entry -> entry.id().equals(id))
                .max(java.util.Comparator.comparing(PluginMarketEntry::publishedAt));
    }

    /**
     * {@code disable} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    public void disable(String id) {
        entries.replaceAll(
                (key, value) ->
                        value.id().equals(id)
                                ? new PluginMarketEntry(
                                        value.id(),
                                        value.version(),
                                        value.source(),
                                        value.manifest(),
                                        value.signature(),
                                        value.publisherKey(),
                                        value.permissions(),
                                        value.checksum(),
                                        value.updatePolicy(),
                                        value.publishedAt(),
                                        false)
                                : value);
    }

    private static String key(String id, String version) {
        return id + "\u0000" + version;
    }
}
