package com.shiyu.ai.tooling.implementation.plugin.market.adapter;
import com.shiyu.ai.tooling.implementation.plugin.market.model.PluginMarketEntry;
import com.shiyu.ai.tooling.implementation.plugin.market.port.PluginMarketStore;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理 In 记忆 插件 Market 相关的运行时状态、注册信息或临时数据。
 */
@Component
@ConditionalOnMissingBean(PluginMarketStore.class)
public class InMemoryPluginMarketStore implements PluginMarketStore {
    private final ConcurrentHashMap<String, PluginMarketEntry> entries = new ConcurrentHashMap<>();

    /**
     * 创建或保存 In 记忆 插件 Market 相关业务数据，并返回处理结果。
     *
     * @param entry 用于完成本次业务处理的 entry 参数。
     * @return 返回 In 记忆 插件 Market 相关操作生成的结果数据。
     */
    @Override
    public PluginMarketEntry save(PluginMarketEntry entry) {
        String key = key(entry.id(), entry.version());
        if (entries.putIfAbsent(key, entry) != null)
            throw new IllegalStateException("plugin version already exists");
        return entry;
    }

    /**
     * 查询 In 记忆 插件 Market 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<PluginMarketEntry> list() {
        return entries.values().stream()
                .sorted(java.util.Comparator.comparing(PluginMarketEntry::id))
                .toList();
    }

    /**
     * 查询 In 记忆 插件 Market 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    @Override
    public Optional<PluginMarketEntry> find(String id) {
        return entries.values().stream()
                .filter(entry -> entry.id().equals(id))
                .max(java.util.Comparator.comparing(PluginMarketEntry::publishedAt));
    }

    /**
     * 更新或设置 In 记忆 插件 Market 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param id 用于定位目标业务对象的标识。
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
