package com.shiyu.ai.dal.knowledge.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("knowledge_review_record")
public class KnowledgeReviewRecordDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;
    private Long documentId;
    private Long versionId;
    private String action;
    private String commentText;
}
