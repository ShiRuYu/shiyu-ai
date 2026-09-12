package com.shiyu.ai.agent.implementation.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * {@code AgentDetailVO} 承载智能体模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentDetailVO {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;

    /**
     * agentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String agentId;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;

    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;

    /**
     * currentVersion 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String currentVersion;

    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;

    /** 扩展字段：该 Agent 当前版本所需的接口入参定义 */
    private Map<String, Object> extInfo;

    /**
     * versions 属性，保存当前对象中的业务数据或协作依赖。
     */
    private List<AgentVersionVO> versions;

    /**
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;

    /**
     * updateTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime updateTime;
}
