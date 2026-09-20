package com.shiyu.ai.tooling.implementation.plugin.market.service;
import com.shiyu.ai.tooling.implementation.plugin.market.adapter.InMemoryPluginMarketStore;
import com.shiyu.ai.tooling.implementation.plugin.market.model.PluginMarketEntry;
import com.shiyu.ai.tooling.implementation.plugin.market.port.PluginMarketStore;

import com.shiyu.ai.tooling.implementation.plugin.security.PluginSignatureVerifier;
import com.shiyu.ai.tooling.implementation.plugin.security.TrustedPublisherRegistry;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * 提供 插件 Market 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Service
public class PluginMarketService {
    /**
     * 存储，表示当前对象中的对应属性。
     */
    private final PluginMarketStore store;
    /**
     * publishers 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final TrustedPublisherRegistry publishers;
    /**
     * developmentMode 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final boolean developmentMode;

    /**
     * 执行 插件 Market 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param stores 用于完成本次业务处理的 stores 参数。
     * @param developmentMode 用于完成本次业务处理的 developmentMode 参数。
     */
    public PluginMarketService(
            ObjectProvider<PluginMarketStore> stores,
            @Value("${shiyu.plugins.development-mode:false}") boolean developmentMode) {
        this.store = stores.getIfAvailable(InMemoryPluginMarketStore::new);
        this.publishers = new TrustedPublisherRegistry();
        this.developmentMode = developmentMode;
    }

    /**
     * 发布或发送 插件 Market 相关业务数据，并返回处理结果。
     *
     * @param entry 用于完成本次业务处理的 entry 参数。
     * @param developmentMode 用于完成本次业务处理的 developmentMode 参数。
     * @return 返回 插件 Market 相关操作生成的结果数据。
     */
    public PluginMarketEntry publish(PluginMarketEntry entry, boolean developmentMode) {
        boolean allowUnsigned = this.developmentMode && developmentMode;
        boolean signed =
                PluginSignatureVerifier.verify(
                        entry.manifest(), entry.signature(), entry.publisherKey());
        if (!signed && !allowUnsigned)
            throw new SecurityException("unsigned or invalid plugin manifest");
        if (signed && !allowUnsigned && !publishers.isTrusted(entry.publisherKey())) {
            throw new SecurityException("plugin publisher is not trusted");
        }
        String checksum = PluginSignatureVerifier.sha256(entry.manifest());
        if (entry.checksum() != null
                && !entry.checksum().isBlank()
                && !checksum.equalsIgnoreCase(entry.checksum())) {
            throw new SecurityException("plugin manifest checksum mismatch");
        }
        PluginMarketEntry stored =
                new PluginMarketEntry(
                        entry.id(),
                        entry.version(),
                        entry.source(),
                        entry.manifest(),
                        entry.signature(),
                        entry.publisherKey(),
                        entry.permissions(),
                        checksum,
                        entry.updatePolicy(),
                        Instant.now(),
                        true);
        return store.save(stored);
    }

    /**
     * 查询 插件 Market 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<PluginMarketEntry> list() {
        return store.list();
    }

    /**
     * 查询 插件 Market 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 插件 Market 相关操作生成的结果数据。
     */
    public PluginMarketEntry find(String id) {
        return store.find(id).orElse(null);
    }

    /**
     * 更新或设置 插件 Market 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param id 用于定位目标业务对象的标识。
     */
    public void disable(String id) {
        store.disable(id);
    }
}
