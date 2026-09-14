package com.shiyu.ai.model.contract.api;

/**
 * ModelCatalogPort 边界接口，负责向外部组件提供模型领域相关能力。
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
