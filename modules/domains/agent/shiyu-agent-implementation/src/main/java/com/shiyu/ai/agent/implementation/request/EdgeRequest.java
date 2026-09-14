package com.shiyu.ai.agent.implementation.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import java.util.Map;

/**
 * {@code EdgeRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class EdgeRequest {

    /**
     * sourceNodeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "源节点不能为空")
    private String sourceNodeId;

    /**
     * targetNodeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "目标节点不能为空")
    private String targetNodeId;

    /**
     * edgeType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String edgeType;

    /**
     * conditionMappings 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Map<String, String> conditionMappings;

    /**
     * defaultTarget 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String defaultTarget;

    /**
     * conditionType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String conditionType;
}
