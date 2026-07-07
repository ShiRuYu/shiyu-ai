package com.shiyu.ai.dal.dataobject.agent;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "intent_def")
public class IntentDefDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String agentId;
    private String code;
    private String name;

    private String description;
    private String category;
    private Integer priority;
    private Double confidenceThreshold;
    private String examples;        // JSON array
    private String targetNode;
    private String requireSlotFilling;  // '1' or '0'
    private String slots;            // JSON object
    private String parameterMapping; // JSON object
    private String slotDefaults;     // JSON object
    private String enabled;          // '1' or '0'
}
