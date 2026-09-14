package com.shiyu.ai.agent.implementation.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import java.util.Map;

/**
 * {@code NodeConfigRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class NodeConfigRequest {

    /**
     * nodeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "节点标识不能为空")
    private String nodeId;

    /**
     * nodeName 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "节点名称不能为空")
    private String nodeName;

    /**
     * nodeType 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "节点类型不能为空")
    private String nodeType;

    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;

    /**
     * 启用开关，表示当前对象中的对应属性。
     */
    private Boolean enabled;

    /**
     * timeout 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long timeout;

    /**
     * retryCount 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer retryCount;

    /**
     * retryInterval 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long retryInterval;

    /**
     * errorStrategy 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String errorStrategy;

    /**
     * logLevel 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String logLevel;

    /**
     * 配置属性，表示当前对象中的对应属性。
     */
    private Map<String, Object> properties;

    /**
     * 配置，表示当前对象中的对应属性。
     */
    private Map<String, Object> config;
}
