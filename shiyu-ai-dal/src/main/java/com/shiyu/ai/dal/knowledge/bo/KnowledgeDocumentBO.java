package com.shiyu.ai.dal.knowledge.bo;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDocumentDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AutoMapper(target = KnowledgeDocumentDO.class, reverseConvertGenerate = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeDocumentBO extends TenantEntity {

    private Long id;
    private String title;
    private String content;
    private String docType;
    private String source;
    private String author;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;

}
