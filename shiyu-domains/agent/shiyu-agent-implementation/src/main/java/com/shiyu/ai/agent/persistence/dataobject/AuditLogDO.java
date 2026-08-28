package com.shiyu.ai.agent.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import com.shiyu.ai.agent.domain.model.AuditLogBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@Table("observation_audit_log")
@AutoMapper(target = AuditLogBO.class, reverseConvertGenerate = true)
public class AuditLogDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long tenantId;
    private Long userId;
    private String action;
    private String targetType;
    private String targetId;
    private String detail;
    private String ip;
    private String result;
    private String errorMsg;
    private Long durationMs;
    private LocalDateTime createTime;
}
