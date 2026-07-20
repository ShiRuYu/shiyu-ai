package com.shiyu.ai.dal.knowledge.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table("knowledge_doc_relation")
public class KnowledgeDocRelationDO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    private Long docId;
    private Long knowledgeId;
    private String relationType;
    private LocalDateTime createTime;
}