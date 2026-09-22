package com.shiyu.ai.common.foundation.database;

import java.util.Collection;
import java.util.Set;

/**
 * 向 数据库 Baseline 所属的应用或基础设施注册必要的扩展能力。
 */
public interface DatabaseBaselineContributor {

    /**
     * 执行 数据库 Baseline 相关业务数据，并返回处理结果。
     *
     * @return 返回 数据库 Baseline 相关操作生成的结果数据。
     */
    default int order() {
        return 0;
    }

    /**
     * 执行 数据库 Baseline 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    Collection<String> schemaResources();

    /**
     * 执行 数据库 Baseline 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    Collection<String> seedResources();

    /**
     * 执行 数据库 Baseline 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    Set<String> expectedTables();
}
