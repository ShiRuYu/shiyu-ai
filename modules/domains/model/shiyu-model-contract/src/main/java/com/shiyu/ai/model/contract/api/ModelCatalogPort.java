package com.shiyu.ai.model.contract.api;

/**
 * 定义 模型 Catalog 领域与外部能力交互的端口契约。
 */
public interface ModelCatalogPort {
    /**
     * 处理countenabledplatforms。
     *
     * @return 受影响的记录数或生成的序号。
     */
    long countEnabledPlatforms();

    /**
     * 处理countenabledmodels。
     *
     * @return 受影响的记录数或生成的序号。
     */
    long countEnabledModels();
}
