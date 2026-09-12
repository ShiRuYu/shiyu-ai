package com.shiyu.ai.common.vector.factory;

import com.shiyu.ai.common.vector.api.VectorStore;
import com.shiyu.ai.common.vector.api.VectorStoreProvider;
import com.shiyu.ai.common.vector.config.VectorStoreProperties;
import com.shiyu.ai.common.vector.model.VectorStoreOptions;

import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ConfiguredVectorStoreProvider 边界接口，负责向外部组件提供基础设施领域相关能力。
 */
public final class ConfiguredVectorStoreProvider implements VectorStoreProvider {

    /**
     * defaults 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final VectorStoreProperties defaults;
    /**
     * JDBC模板，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbcTemplate;
    private final Map<String, InMemoryHandle> inMemoryStores = new ConcurrentHashMap<>();

    /**
     * {@code ConfiguredVectorStoreProvider} 创建并初始化当前类型实例。
     *
     * @param defaults 参数值，用于执行当前操作。
     */
    public ConfiguredVectorStoreProvider(VectorStoreProperties defaults) {
        this(defaults, null);
    }

    /**
     * {@code ConfiguredVectorStoreProvider} 创建并初始化当前类型实例。
     *
     * @param defaults 参数值，用于执行当前操作。
     * @param jdbcTemplate 参数值，用于执行当前操作。
     */
    public ConfiguredVectorStoreProvider(
            VectorStoreProperties defaults, JdbcTemplate jdbcTemplate) {
        this.defaults = defaults;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * {@code type} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String type() {
        return defaults.getType().toLowerCase(Locale.ROOT);
    }

    /**
     * {@code open} 执行当前类型定义的业务操作。
     *
     * @param options 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public VectorStore open(VectorStoreOptions options) {
        if ("inmemory".equals(type())) {
            InMemoryHandle handle =
                    inMemoryStores.compute(
                            options.namespace(),
                            (namespace, existing) -> {
                                if (existing != null
                                        && existing.dimension() != options.dimension()) {
                                    throw new IllegalArgumentException(
                                            "Vector dimension mismatch for namespace "
                                                    + namespace
                                                    + ": expected "
                                                    + existing.dimension()
                                                    + ", actual "
                                                    + options.dimension());
                                }
                                return existing != null
                                        ? existing
                                        : new InMemoryHandle(options.dimension(), create(options));
                            });
            return handle.store();
        }
        return create(options);
    }

    /**
     * {@code drop} 执行当前类型定义的业务操作。
     *
     * @param options 参数值，用于执行当前操作。
     */
    @Override
    public void drop(VectorStoreOptions options) {
        if ("pgvector".equals(type())) {
            new com.shiyu.ai.common.vector.implementation.PgVectorStore(
                            jdbcTemplate, options.namespace(), options.dimension())
                    .rebuild();
            return;
        }
        InMemoryHandle handle = inMemoryStores.remove(options.namespace());
        if (handle != null) {
            handle.store().rebuild();
            handle.store().close();
        }
    }

    /**
     * {@code close} 释放或移除当前操作涉及的资源。
     */
    @Override
    public void close() {
        inMemoryStores.values().forEach(handle -> handle.store().close());
        inMemoryStores.clear();
    }

    private VectorStore create(VectorStoreOptions options) {
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setType(type());
        properties.setDimension(options.dimension());
        properties.setDataDir(resolveDataDir(options));
        if ("pgvector".equals(type())) {
            return new com.shiyu.ai.common.vector.implementation.PgVectorStore(
                    jdbcTemplate, options.namespace(), options.dimension());
        }
        return VectorStoreFactory.create(type(), properties, jdbcTemplate);
    }

    private String resolveDataDir(VectorStoreOptions options) {
        if (options.dataDir() != null && !options.dataDir().isBlank()) {
            return options.dataDir();
        }
        String safeNamespace = options.namespace().replaceAll("[^a-zA-Z0-9._-]+", "_");
        return Path.of(defaults.getResolvedDataDir(), safeNamespace).toString();
    }

    /**
     * {@code InMemoryHandle} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param dimension 向量维度，表示该记录组件承载的数据。
     * @param store store 属性，表示该记录组件承载的数据。
     */
    private record InMemoryHandle(int dimension, VectorStore store) {}
}
