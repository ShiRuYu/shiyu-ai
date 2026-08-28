package com.shiyu.ai.knowledge.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphEdge implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long targetId;

    private String type;

    private Double weight;
}
