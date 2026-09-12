package com.shiyu.ai.model.contract.api;

import java.util.List;

/**
 * ModelRoutingPort 边界接口，负责向外部组件提供模型领域相关能力。
 */
public interface ModelRoutingPort {
    /**
     * 处理availablemodels。
     *
     * @return 结果列表。
     */
    List<ModelDescriptor> availableModels();

    /**
     * 解析平台。
     *
     * @param model model 参数。
     *
     * @return 处理结果。
     */
    String resolvePlatform(String model);

    /**
     * 处理默认平台。
     *
     * @return 处理结果。
     */
    String defaultPlatform();

    /**
     * 描述可路由模型的标识和所属平台。
     * @param id 标识，表示该记录组件承载的数据。
     * @param platform platform 属性，表示该记录组件承载的数据。
     */
    record ModelDescriptor(String id, String platform) {}
}
