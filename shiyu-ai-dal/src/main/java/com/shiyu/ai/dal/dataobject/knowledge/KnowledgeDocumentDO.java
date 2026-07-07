package com.shiyu.ai.dal.dataobject.knowledge;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("knowledge_document")
public class KnowledgeDocumentDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String title;
    private String content;
    private String docType;
    private String source;
    private String author;
}
