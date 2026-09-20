package com.shiyu.ai.agent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shiyu.ai.agent.implementation.graph.Graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实现 智能体 Version 相关的业务处理、协作逻辑或基础设施能力。
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
