package com.shiyu.ai.knowledge.implementation.domain.model;

import com.shiyu.ai.common.core.domain.TenantModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * {@code KnowledgeIngestionJobBO} 是知识模块的业务对象，承载用例处理所需的领域数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class KnowledgeIngestionJobBO extends TenantModel {
    /**
     * 标识，表示当前对象中的对应属性。
     */
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

    /** 调用方用户标识。 */
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
