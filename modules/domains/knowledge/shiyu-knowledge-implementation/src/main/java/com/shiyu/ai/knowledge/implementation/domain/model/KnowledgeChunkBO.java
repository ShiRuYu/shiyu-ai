package com.shiyu.ai.knowledge.implementation.domain.model;

import com.shiyu.ai.common.core.domain.TenantModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * {@code KnowledgeChunkBO} 是知识模块的业务对象，承载用例处理所需的领域数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class KnowledgeChunkBO extends TenantModel {

    /**
     * 标识，表示当前对象中的对应属性。
     */
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

    /** 状态（依据业务灵活定义） */
    private Integer status;
}
