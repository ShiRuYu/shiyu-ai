package com.shiyu.ai.knowledge.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocRelationBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("knowledge_doc_relation")
@AutoMapper(target = KnowledgeDocRelationBO.class, reverseConvertGenerate = true)
public class KnowledgeDocRelationDO extends TenantEntity {
    private static final long serialVersionUID = 1L;
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    private Long spaceId;
    private Long docId;
    private Long knowledgeId;
    private String relationType;
    private LocalDateTime createTime;
}

