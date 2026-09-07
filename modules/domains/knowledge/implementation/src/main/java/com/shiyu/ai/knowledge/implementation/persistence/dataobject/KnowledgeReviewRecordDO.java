package com.shiyu.ai.knowledge.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.shiyu.ai.knowledge.domain.model.KnowledgeReviewRecordBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
@Table("knowledge_review_record")
@AutoMapper(target = KnowledgeReviewRecordBO.class, reverseConvertGenerate = true)
public class KnowledgeReviewRecordDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;
    private Long documentId;
    private Long versionId;
    private String action;
    private String commentText;
}

