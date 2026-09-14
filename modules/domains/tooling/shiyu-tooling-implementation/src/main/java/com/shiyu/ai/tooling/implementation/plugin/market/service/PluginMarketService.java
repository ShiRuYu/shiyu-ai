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
 * PluginMarketService 服务接口，负责执行工具领域相关业务操作。
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
     * {@code PluginMarketService} 创建并初始化当前类型实例。
     *
     * @param stores 参数值，用于执行当前操作。
     * @param developmentMode 参数值，用于执行当前操作。
     */
    public PluginMarketService(
            ObjectProvider<PluginMarketStore> stores,
            @Value("${shiyu.plugins.development-mode:false}") boolean developmentMode) {
        this.store = stores.getIfAvailable(InMemoryPluginMarketStore::new);
        this.publishers = new TrustedPublisherRegistry();
        this.developmentMode = developmentMode;
    }

    /**
     * {@code publish} 执行当前模块定义的业务流程。
     *
     * @param entry 参数值，用于执行当前操作。
     * @param developmentMode 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<PluginMarketEntry> list() {
        return store.list();
    }

    /**
     * {@code find} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public PluginMarketEntry find(String id) {
        return store.find(id).orElse(null);
    }

    /**
     * {@code disable} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     */
    public void disable(String id) {
        store.disable(id);
    }
}
