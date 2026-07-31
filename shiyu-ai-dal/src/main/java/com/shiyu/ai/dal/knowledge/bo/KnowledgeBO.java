package com.shiyu.ai.dal.knowledge.bo;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@AutoMapper(target = KnowledgeDO.class, reverseConvertGenerate = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeBO extends TenantEntity {

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
