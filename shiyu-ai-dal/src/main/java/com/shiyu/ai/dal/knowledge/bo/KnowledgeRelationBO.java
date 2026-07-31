package com.shiyu.ai.dal.knowledge.bo;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeRelationDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AutoMapper(target = KnowledgeRelationDO.class, reverseConvertGenerate = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeRelationBO extends TenantEntity {

    private Long id;
    private Long spaceId;
    private Long sourceId;
    private Long targetId;
    private String relationType;
    private Double weight;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;

}
