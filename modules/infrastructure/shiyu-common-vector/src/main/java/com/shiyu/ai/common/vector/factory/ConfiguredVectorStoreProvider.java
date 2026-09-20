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
 * 创建或提供 Configured 向量 Store 相关的业务组件和运行时能力。
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
     * 执行 Configured 向量 Store 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param defaults 用于完成本次业务处理的 defaults 参数。
     */
    public ConfiguredVectorStoreProvider(VectorStoreProperties defaults) {
        this(defaults, null);
    }

    /**
     * 执行 Configured 向量 Store 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param defaults 用于完成本次业务处理的 defaults 参数。
     * @param jdbcTemplate 用于完成本次业务处理的 jdbcTemplate 参数。
     */
    public ConfiguredVectorStoreProvider(
            VectorStoreProperties defaults, JdbcTemplate jdbcTemplate) {
        this.defaults = defaults;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 执行 Configured 向量 Store 相关业务数据，并返回处理结果。
     *
     * @return 返回 Configured 向量 Store 相关操作生成的结果数据。
     */
    @Override
    public String type() {
        return defaults.getType().toLowerCase(Locale.ROOT);
    }

    /**
     * 创建或保存 Configured 向量 Store 相关业务数据，并返回处理结果。
     *
     * @param options 用于完成本次业务处理的 options 参数。
     * @return 返回 Configured 向量 Store 相关操作生成的结果数据。
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
     * 执行 Configured 向量 Store 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param options 用于完成本次业务处理的 options 参数。
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
     * 封装 In 记忆 Handle 相关的不可变数据及其字段约束。
     */
    private record InMemoryHandle(int dimension, VectorStore store) {}
}
