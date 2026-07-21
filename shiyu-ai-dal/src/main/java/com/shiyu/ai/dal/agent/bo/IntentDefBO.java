package com.shiyu.ai.dal.agent.bo;

import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;
import java.util.List;
import java.util.Map;
import io.github.linpeilie.annotations.AutoMapping;
import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.agent.dataobject.IntentDefDO;

/**
 * 意图定义业务对象
 */
@AutoMapper(target = IntentDefDO.class, reverseConvertGenerate = false)
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
    @AutoMapping(ignore = true)
    private List<String> examples;
    private String targetNode;
    @AutoMapping(ignore = true)
    private Boolean requireSlotFilling;
    @AutoMapping(ignore = true)
    private Map<String, String> slots;
    @AutoMapping(ignore = true)
    private Map<String, String> parameterMapping;
    @AutoMapping(ignore = true)
    private Map<String, String> slotDefaults;
    @AutoMapping(ignore = true)
    private Boolean enabled;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;

}
