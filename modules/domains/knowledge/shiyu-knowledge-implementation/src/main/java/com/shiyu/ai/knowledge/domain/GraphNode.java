package com.shiyu.ai.knowledge.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("serial")
public class GraphNode implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String code;

    private List<Long> parentIds;

    private List<Long> childIds;

    private List<Long> relatedIds;

    private List<GraphEdge> edges;

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
