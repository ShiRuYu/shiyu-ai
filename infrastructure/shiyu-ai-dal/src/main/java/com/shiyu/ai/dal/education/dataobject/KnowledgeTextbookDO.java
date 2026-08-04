package com.shiyu.ai.dal.education.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import com.shiyu.ai.education.domain.model.KnowledgeTextbookBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@Table("edu_knowledge_textbook")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = KnowledgeTextbookBO.class, reverseConvertGenerate = true)
public class KnowledgeTextbookDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long knowledgeId;
    private Long textbookId;
    private Long chapterId;
}
