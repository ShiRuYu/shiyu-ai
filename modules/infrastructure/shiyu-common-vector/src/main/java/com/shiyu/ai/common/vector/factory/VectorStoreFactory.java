package com.shiyu.ai.common.vector.factory;

import com.shiyu.ai.common.vector.api.VectorStore;
import com.shiyu.ai.common.vector.config.VectorStoreProperties;
import com.shiyu.ai.common.vector.implementation.InMemoryVectorStore;
import com.shiyu.ai.common.vector.implementation.JVectorStore;

import lombok.extern.slf4j.Slf4j;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * VectorStore 工厂
 *
 * <p>根据配置类型创建对应的 VectorStore 实例。 新增实现时只需在此 switch 中添加分支，无需额外接口或注册文件。
 */
@Slf4j
public class VectorStoreFactory {

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param type 参数值，用于执行当前操作。
     * @param properties 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static VectorStore create(String type, VectorStoreProperties properties) {
        return create(type, properties, null);
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param type 参数值，用于执行当前操作。
     * @param properties 参数值，用于执行当前操作。
     * @param jdbcTemplate 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
