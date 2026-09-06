package com.shiyu.ai.knowledge.domain.model;
import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class KnowledgeIngestionJobBO extends TenantModel {
    private Long id;
    private String jobKey;
    private String jobType;
    private Long spaceId;
    private Long documentId;
    private Long versionId;
    /** User who submitted the job; used to attribute embedding usage. */
    private Long actorUserId;
    private String jobStatus;
    private String stage;
    private Integer progress;
    private Integer attempts;
    private Integer maxAttempts;
    private String errorMessage;
    private String checkpointData;
    private LocalDateTime heartbeatTime;
    private LocalDateTime startedTime;
    private LocalDateTime finishedTime;
    private Long lockVersion;
}
