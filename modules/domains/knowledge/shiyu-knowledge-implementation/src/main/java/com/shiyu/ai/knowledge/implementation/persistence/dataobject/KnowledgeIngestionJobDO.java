package com.shiyu.ai.knowledge.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeIngestionJobBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * {@code KnowledgeIngestionJobDO} 是知识模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
@Table("knowledge_ingestion_job")
@AutoMapper(target = KnowledgeIngestionJobBO.class, reverseConvertGenerate = true)
public class KnowledgeIngestionJobDO extends TenantEntity {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * jobKey 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String jobKey;
    /**
     * jobType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String jobType;
    /**
     * spaceId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long spaceId;
    /**
     * documentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long documentId;
    /**
     * 版本标识，表示当前对象中的对应属性。
     */
    private Long versionId;
    /**
     * actorUserId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long actorUserId;
    /**
     * jobStatus 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String jobStatus;
    /**
     * stage 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String stage;
    /**
     * progress 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer progress;
    /**
     * attempts 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer attempts;
    /**
     * maxAttempts 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer maxAttempts;
    /**
     * 错误消息，表示当前对象中的对应属性。
     */
    private String errorMessage;
    /**
     * checkpointData 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String checkpointData;
    /**
     * heartbeatTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime heartbeatTime;
    /**
     * startedTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime startedTime;
    /**
     * finishedTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime finishedTime;
    /**
     * 锁版本，表示当前对象中的对应属性。
     */
    private Long lockVersion;
}
