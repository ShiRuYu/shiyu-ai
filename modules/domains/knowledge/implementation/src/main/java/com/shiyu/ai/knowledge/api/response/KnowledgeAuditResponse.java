package com.shiyu.ai.knowledge.api.response;

import com.shiyu.ai.knowledge.domain.model.KnowledgeAuditLogBO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;

/** Public audit-log representation; persistence BOs never cross the service boundary. */
@Data
@AutoMapper(target = KnowledgeAuditLogBO.class)
public class KnowledgeAuditResponse {
    private Long id;
    private Long tenantId;
    private Long spaceId;
    private String resourceType;
    private Long resourceId;
    private String action;
    private String detailJson;
    private Integer status;
    private Integer delFlag;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
