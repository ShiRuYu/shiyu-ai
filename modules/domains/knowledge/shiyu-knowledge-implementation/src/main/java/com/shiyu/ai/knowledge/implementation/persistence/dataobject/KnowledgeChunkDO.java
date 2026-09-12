package com.shiyu.ai.knowledge.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeChunkBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * {@code KnowledgeChunkDO} 是知识模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
@Table("vector_knowledge_chunk")
@AutoMapper(target = KnowledgeChunkBO.class, reverseConvertGenerate = true)
public class KnowledgeChunkDO extends TenantEntity {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * documentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long documentId;

    /**
     * spaceId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long spaceId;

    /**
     * 版本标识，表示当前对象中的对应属性。
     */
    private Long versionId;

    /**
     * 内容，表示当前对象中的对应属性。
     */
    private String content;

    /**
     * 嵌入向量，表示当前对象中的对应属性。
     */
    private String embedding;

    /**
     * embeddingBinary 属性，保存当前对象中的业务数据或协作依赖。
     */
    private byte[] embeddingBinary;

    /**
     * embeddingModel 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String embeddingModel;

    /**
     * 嵌入向量维度，表示当前对象中的对应属性。
     */
    private Integer embeddingDimension;

    /**
     * 元数据，表示当前对象中的对应属性。
     */
    private String metadata;

    /**
     * chunkIndex 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer chunkIndex;

    /**
     * pageNumber 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer pageNumber;

    /**
     * 小节路径，表示当前对象中的对应属性。
     */
    private String sectionPath;

    /**
     * startOffset 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer startOffset;

    /**
     * endOffset 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer endOffset;

    /**
     * 令牌数量，表示当前对象中的对应属性。
     */
    private Integer tokenCount;
}
