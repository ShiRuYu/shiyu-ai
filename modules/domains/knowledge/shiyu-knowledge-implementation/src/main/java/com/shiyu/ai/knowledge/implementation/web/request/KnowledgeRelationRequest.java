package com.shiyu.ai.knowledge.implementation.web.request;

import com.shiyu.ai.knowledge.implementation.domain.RelationType;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code KnowledgeRelationRequest} 表示知识模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class KnowledgeRelationRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 来源标识，表示当前对象中的对应属性。
     */
    @NotNull(message = "源知识点ID不能为空")
    private Long sourceId;

    /**
     * 目标标识，表示当前对象中的对应属性。
     */
    @NotNull(message = "目标知识点ID不能为空")
    private Long targetId;

    /**
     * 类型，表示当前对象中的对应属性。
     */
    @NotNull(message = "关系类型不能为空")
    private RelationType type;

    /**
     * 权重，表示当前对象中的对应属性。
     */
    private Double weight = 1.0;
}
