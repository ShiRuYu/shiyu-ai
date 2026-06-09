package com.shiyu.ai.agent.domain.bo;

import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;
import java.util.List;
import java.util.Map;

/**
 * 意图定义业务对象
 * 注意: JSON字段（examples/slots/parameterMapping/slotDefaults）由 Repository 手动转换
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IntentDefBO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String agentId;
    private String code;
    private String name;
    private String description;
    private String category;
    private Integer priority;
    private Double confidenceThreshold;
    private List<String> examples;
    private String targetNode;
    private Boolean requireSlotFilling;
    private Map<String, String> slots;
    private Map<String, String> parameterMapping;
    private Map<String, String> slotDefaults;
    private Boolean enabled;
    private String status;
    private String delFlag;
}
