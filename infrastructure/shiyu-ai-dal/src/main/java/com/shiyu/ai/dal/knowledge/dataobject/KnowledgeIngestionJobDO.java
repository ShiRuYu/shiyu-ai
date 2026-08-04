package com.shiyu.ai.dal.knowledge.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import com.shiyu.ai.knowledge.domain.model.KnowledgeIngestionJobBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("knowledge_ingestion_job")
@AutoMapper(target = KnowledgeIngestionJobBO.class, reverseConvertGenerate = true)
public class KnowledgeIngestionJobDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;
    private String jobKey;
    private String jobType;
    private Long spaceId;
    private Long documentId;
    private Long versionId;
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
