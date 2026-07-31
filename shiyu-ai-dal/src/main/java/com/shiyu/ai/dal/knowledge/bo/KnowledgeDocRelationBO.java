package com.shiyu.ai.dal.knowledge.bo;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDocRelationDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

@AutoMapper(target = KnowledgeDocRelationDO.class, reverseConvertGenerate = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeDocRelationBO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long spaceId;
    private Long docId;
    private Long knowledgeId;
    private String relationType;
    private LocalDateTime createTime;
}
