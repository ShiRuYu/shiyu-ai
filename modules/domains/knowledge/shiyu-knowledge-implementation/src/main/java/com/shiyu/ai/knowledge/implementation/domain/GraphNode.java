package com.shiyu.ai.knowledge.implementation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code GraphNode} 承载知识模块中的智能流程节点，负责执行本节点的输入处理与结果产出。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("serial")
public class GraphNode implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;

    /**
     * parentIds 属性，保存当前对象中的业务数据或协作依赖。
     */
    private List<Long> parentIds;

    /**
     * childIds 属性，保存当前对象中的业务数据或协作依赖。
     */
    private List<Long> childIds;

    /**
     * relatedIds 属性，保存当前对象中的业务数据或协作依赖。
     */
    private List<Long> relatedIds;

    /**
     * edges 属性，保存当前对象中的业务数据或协作依赖。
     */
    private List<GraphEdge> edges;

    /**
     * {@code of} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param name 参数值，用于执行当前操作。
     * @param code 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static GraphNode of(Long id, String name, String code) {
        return GraphNode.builder()
                .id(id)
                .name(name)
                .code(code)
                .parentIds(new ArrayList<>())
                .childIds(new ArrayList<>())
                .relatedIds(new ArrayList<>())
                .edges(new ArrayList<>())
                .build();
    }
}
