package com.shiyu.ai.dal.education.dataobject;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@Table("resource_knowledge")
@EqualsAndHashCode(callSuper = true)
public class ResourceKnowledgeDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long resourceId;
    private Long knowledgeId;
    private Integer sortOrder;
}
