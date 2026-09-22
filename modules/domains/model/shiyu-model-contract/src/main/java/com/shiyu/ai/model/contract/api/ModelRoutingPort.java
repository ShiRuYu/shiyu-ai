package com.shiyu.ai.model.contract.api;

import java.util.List;

/**
 * 提供模型路由查询端口，解析可用模型、模型所属平台和默认平台。
 */
public interface ModelRoutingPort {
    /**
     * 返回当前运行环境可用的模型描述。
     *
     * @return 可用模型列表；没有可用模型时返回空列表。
     */
    List<ModelDescriptor> availableModels();

    /**
     * 根据模型标识解析其对应的平台编码。
     *
     * @param model 模型标识。
     *
     * @return 模型所属平台编码；模型未注册时返回空值。
     */
    String resolvePlatform(String model);

    /**
     * 查询系统为模型调用选择的默认平台编码。
     *
     * @return 默认平台编码；未配置默认平台时返回空值。
     */
    String defaultPlatform();

    /**
     * 封装 模型 Descriptor 相关的不可变数据及其字段约束。
     */
    record ModelDescriptor(String id, String platform) {}
}
