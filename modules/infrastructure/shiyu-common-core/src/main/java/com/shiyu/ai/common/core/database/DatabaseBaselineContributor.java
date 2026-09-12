package com.shiyu.ai.common.core.database;

import java.util.Collection;
import java.util.Set;

/**
 * DatabaseBaselineContributor 接口，定义基础设施模块的能力边界。
 */
public interface DatabaseBaselineContributor {

    /**
     * 执行 {@code order} 定义的接口操作。
     *
     * @return 操作影响的记录数或状态码。
     */
    default int order() {
        return 0;
    }

    /**
     * 执行 {@code schemaResources} 定义的接口操作。
     *
     * @return 符合条件的结果集合。
     */
    Collection<String> schemaResources();

    /**
     * 执行 {@code seedResources} 定义的接口操作。
     *
     * @return 符合条件的结果集合。
     */
    Collection<String> seedResources();

    /**
     * 执行 {@code expectedTables} 定义的接口操作。
     *
     * @return 符合条件的结果集合。
     */
    Set<String> expectedTables();
}
