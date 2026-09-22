package com.shiyu.ai.model.contract.api;

/**
 * 提供模型目录统计端口，查询当前目录中已启用的平台数量和模型数量。
 */
public interface ModelCatalogPort {
    /**
     * 统计已启用的模型平台数量。
     *
     * @return 已启用的平台记录数。
     */
    long countEnabledPlatforms();

    /**
     * 统计已启用的模型数量。
     *
     * @return 已启用的模型记录数。
     */
    long countEnabledModels();
}
