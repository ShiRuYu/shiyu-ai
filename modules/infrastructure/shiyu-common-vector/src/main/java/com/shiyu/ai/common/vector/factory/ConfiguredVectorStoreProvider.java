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

/** Default provider backed by the configured vector implementation. */
public final class ConfiguredVectorStoreProvider implements VectorStoreProvider {

    private final VectorStoreProperties defaults;
    private final JdbcTemplate jdbcTemplate;
    private final Map<String, InMemoryHandle> inMemoryStores = new ConcurrentHashMap<>();

    public ConfiguredVectorStoreProvider(VectorStoreProperties defaults) {
        this(defaults, null);
    }

    public ConfiguredVectorStoreProvider(
            VectorStoreProperties defaults, JdbcTemplate jdbcTemplate) {
        this.defaults = defaults;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String type() {
        return defaults.getType().toLowerCase(Locale.ROOT);
    }

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

    private record InMemoryHandle(int dimension, VectorStore store) {}
}
