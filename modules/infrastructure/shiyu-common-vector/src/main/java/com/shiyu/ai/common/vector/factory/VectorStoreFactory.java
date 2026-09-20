package com.shiyu.ai.common.vector.factory;

import com.shiyu.ai.common.vector.api.VectorStore;
import com.shiyu.ai.common.vector.config.VectorStoreProperties;
import com.shiyu.ai.common.vector.implementation.InMemoryVectorStore;
import com.shiyu.ai.common.vector.implementation.JVectorStore;

import lombok.extern.slf4j.Slf4j;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 创建或提供 向量 Store 相关的业务组件和运行时能力。
 */
@Slf4j
public class VectorStoreFactory {

    /**
     * 创建或保存 向量 Store 相关业务数据，并返回处理结果。
     *
     * @param type 用于完成本次业务处理的 type 参数。
     * @param properties 用于完成本次业务处理的 properties 参数。
     * @return 返回 向量 Store 相关操作生成的结果数据。
     */
    public static VectorStore create(String type, VectorStoreProperties properties) {
        return create(type, properties, null);
    }

    /**
     * 创建或保存 向量 Store 相关业务数据，并返回处理结果。
     *
     * @param type 用于完成本次业务处理的 type 参数。
     * @param properties 用于完成本次业务处理的 properties 参数。
     * @param jdbcTemplate 用于完成本次业务处理的 jdbcTemplate 参数。
     * @return 返回 向量 Store 相关操作生成的结果数据。
     */
    public static VectorStore create(
            String type, VectorStoreProperties properties, JdbcTemplate jdbcTemplate) {
        VectorStore store =
                switch (type.toLowerCase()) {
                    case "inmemory" -> new InMemoryVectorStore(properties.getDimension());
                    case "jvector" -> new JVectorStore(properties);
                    case "pgvector" -> {
                        if (jdbcTemplate == null) {
                            throw new IllegalStateException(
                                    "pgvector requires JdbcTemplate and a PostgreSQL DataSource");
                        }
                        yield new com.shiyu.ai.common.vector.implementation.PgVectorStore(
                                jdbcTemplate, "global/default", properties.getDimension());
                    }
                    default ->
                            throw new IllegalArgumentException(
                                    "未知的 VectorStore 类型: "
                                            + type
                                            + "，可用类型: inmemory, jvector, pgvector");
                };
        log.info(
                "VectorStore 已创建: type={}, class={}",
                store.type(),
                store.getClass().getSimpleName());
        return store;
    }
}
