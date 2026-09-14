package com.shiyu.ai.agent.implementation.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * {@code AgentVersionDetailVO} 承载智能体模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentVersionDetailVO {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;

    /**
     * agentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String agentId;

    /**
     * versionNumber 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String versionNumber;

    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;

    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;

    /**
     * statusDesc 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String statusDesc;

    /**
     * 图结构配置，表示当前对象中的对应属性。
     */
    private GraphConfigVO graphConfig;

    /**
     * canvasConfig 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String canvasConfig;

    /**
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;

    /**
     * updateTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime updateTime;

    /**
     * {@code GraphConfigVO} 承载智能体模块的领域状态或协作行为，负责维护本类型的职责边界。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphConfigVO {
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
        private Map<String, Object> nodes;
        /**
         * edges 属性，保存当前对象中的业务数据或协作依赖。
         */
        private Map<String, Object> edges;
        /**
         * conditionalEdges 属性，保存当前对象中的业务数据或协作依赖。
         */
        private Map<String, Object> conditionalEdges;
    }
}
