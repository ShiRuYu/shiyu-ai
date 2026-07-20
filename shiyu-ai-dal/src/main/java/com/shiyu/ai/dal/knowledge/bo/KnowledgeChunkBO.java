package com.shiyu.ai.dal.knowledge.bo;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeChunkDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AutoMapper(target = KnowledgeChunkDO.class, reverseConvertGenerate = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeChunkBO extends TenantEntity {

    private Long id;
    private Long documentId;
    private String content;
    private String embedding;
    private String metadata;
    private Integer chunkIndex;
}
