package com.shiyu.ai.knowledge.api.response;

import lombok.Data;

import java.time.LocalDateTime;

/** Public audit-log representation; persistence BOs never cross the service boundary. */
@Data
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
