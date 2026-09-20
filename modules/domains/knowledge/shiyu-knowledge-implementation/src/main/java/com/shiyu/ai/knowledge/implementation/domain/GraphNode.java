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
 * 执行 Graph 相关流程节点的输入处理和状态转移。
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
     * 执行 Graph 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回 Graph 相关操作生成的结果数据。
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
