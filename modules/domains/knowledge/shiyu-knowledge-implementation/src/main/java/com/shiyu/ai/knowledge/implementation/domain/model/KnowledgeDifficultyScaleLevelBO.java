package com.shiyu.ai.knowledge.implementation.domain.model;

import com.shiyu.ai.common.core.domain.TenantModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 表示 知识 Difficulty Scale Level 领域对象的业务状态和属性。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class KnowledgeDifficultyScaleLevelBO extends TenantModel {
    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;
    /**
     * scaleId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long scaleId;
    /**
     * 级别，表示当前对象中的对应属性。
     */
    private Integer level;
    /**
     * label 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String label;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;
}
