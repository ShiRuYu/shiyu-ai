package com.shiyu.ai.agent.implementation.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * {@code GraphConfigRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class GraphConfigRequest {

    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;

    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;

    /**
     * startNode 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String startNode;

    /**
     * endNode 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String endNode;

    /**
     * nodes 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Map<String, NodeConfigDTO> nodes;

    /**
     * edges 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Map<String, List<String>> edges;

    /**
     * conditionalEdges 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Map<String, ConditionalEdgeDTO> conditionalEdges;

    /**
     * {@code NodeConfigDTO} 是智能体模块的数据传输对象，用于边界之间传递结构化数据。
     */
    @Data
    public static class NodeConfigDTO {
        /**
         * nodeName 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String nodeName;
        /**
         * 描述，表示当前对象中的对应属性。
         */
        private String description;
        /**
         * nodeType 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String nodeType;
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

    /**
     * {@code ConditionalEdgeDTO} 是智能体模块的数据传输对象，用于边界之间传递结构化数据。
     */
    @Data
    public static class ConditionalEdgeDTO {
        private String defaultTarget;
        /**
         * nodeMappings 属性，保存当前对象中的业务数据或协作依赖。
         */
        private Map<String, String> nodeMappings;
        /**
         * conditionType 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String conditionType;
    }
}
