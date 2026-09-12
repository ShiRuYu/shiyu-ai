package com.shiyu.ai.agent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shiyu.ai.agent.implementation.graph.Graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code AgentVersion} 承载智能体模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentVersion {
    /**
     * versionNumber 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String versionNumber;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;
    /**
     * 图结构，表示当前对象中的对应属性。
     */
    @JsonIgnore private Graph graph;

    /**
     * 创建时间，表示当前对象中的对应属性。
     */
    private long createdAt;
}
