package com.shiyu.ai.model.contract.api;

import java.util.List;

/**
 * 定义 模型 Routing 领域与外部能力交互的端口契约。
 */
public interface ModelRoutingPort {
    /**
     * 执行 模型 Routing 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 封装 模型 Descriptor 相关的不可变数据及其字段约束。
     */
    record ModelDescriptor(String id, String platform) {}
}
