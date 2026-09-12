package com.shiyu.ai.knowledge.implementation.domain.model;

import com.shiyu.ai.common.core.domain.TenantModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * {@code KnowledgeBO} 是知识模块的业务对象，承载用例处理所需的领域数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeBO extends TenantModel {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;
    /**
     * spaceId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long spaceId;
    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;
    /**
     * difficulty 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer difficulty;

    /** 新版统一字段。difficulty 保留用于旧版教育数据兼容，迁移完成后以本字段为准。 */
    private Integer difficultyLevel;

    /**
     * category 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String category;
    /**
     * tags 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String tags;

    /** 状态（依据业务灵活定义） */
    private Integer status;
}
