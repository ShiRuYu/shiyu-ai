package com.shiyu.ai.knowledge.request;

import com.shiyu.ai.knowledge.domain.RelationType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class KnowledgeRelationRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "源知识点ID不能为空")
    private Long sourceId;

    @NotNull(message = "目标知识点ID不能为空")
    private Long targetId;

    @NotNull(message = "关系类型不能为空")
    private RelationType type;

    private Double weight = 1.0;
}
