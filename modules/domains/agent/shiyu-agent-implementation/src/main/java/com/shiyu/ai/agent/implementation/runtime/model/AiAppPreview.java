package com.shiyu.ai.agent.implementation.runtime.model;

import com.shiyu.ai.agent.contract.runtime.*;

import java.util.Map;

/**
 * {@code AiAppPreview} 封装智能体模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param appId appId 属性，表示该记录组件承载的数据。
 * @param appVersionId appVersionId 属性，表示该记录组件承载的数据。
 * @param status 状态，表示该记录组件承载的数据。
 * @param promptHash promptHash 属性，表示该记录组件承载的数据。
 * @param model 模型，表示该记录组件承载的数据。
 * @param configuration configuration 属性，表示该记录组件承载的数据。
 * @param executable executable 属性，表示该记录组件承载的数据。
 */
public record AiAppPreview(
        String appId,
        String appVersionId,
        String status,
        String promptHash,
        String model,
        Map<String, Object> configuration,
        boolean executable) {
    public AiAppPreview {
        configuration = configuration == null ? Map.of() : Map.copyOf(configuration);
    }
}
