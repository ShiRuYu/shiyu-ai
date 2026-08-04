package com.shiyu.ai.knowledge.domain.model;

import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeBO extends TenantModel {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long spaceId;
    private String code;
    private String name;
    private String description;
    private Integer difficulty;
    /**
     * 新版统一字段。difficulty 保留用于旧版教育数据兼容，迁移完成后以本字段为准。
     */
    private Integer difficultyLevel;
    private String category;
    private String tags;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;

}
