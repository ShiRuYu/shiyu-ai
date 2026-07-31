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
    private Long spaceId;
    private Long versionId;
    private String content;
    private String embedding;
    private byte[] embeddingBinary;
    private String embeddingModel;
    private Integer embeddingDimension;
    private String metadata;
    private Integer chunkIndex;
    private Integer pageNumber;
    private String sectionPath;
    private Integer startOffset;
    private Integer endOffset;
    private Integer tokenCount;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;

}
