package com.shiyu.ai.knowledge.implementation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 表示 Graph 相关流程中的状态、关系或执行数据。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphEdge implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 目标标识，表示当前对象中的对应属性。
     */
    private Long targetId;

    /**
     * 类型，表示当前对象中的对应属性。
     */
    private String type;

    /**
     * 权重，表示当前对象中的对应属性。
     */
    private Double weight;
}
