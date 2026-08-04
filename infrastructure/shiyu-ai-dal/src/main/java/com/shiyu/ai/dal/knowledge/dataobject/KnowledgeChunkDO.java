package com.shiyu.ai.dal.knowledge.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("vector_knowledge_chunk")
public class KnowledgeChunkDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
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
}
