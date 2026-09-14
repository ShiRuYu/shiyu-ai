package com.shiyu.ai.agent.implementation.domain.model;

import com.shiyu.ai.common.core.domain.TenantModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;
import java.util.Map;

/** 意图定义业务对象 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class IntentDefBO extends TenantModel {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;
    /**
     * agentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String agentId;
    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;
    /**
     * category 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String category;
    /**
     * priority 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer priority;
    /**
     * confidenceThreshold 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double confidenceThreshold;
    /**
     * examples 属性，保存当前对象中的业务数据或协作依赖。
     */
    private List<String> examples;
    /**
     * targetNode 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String targetNode;
    /**
     * requireSlotFilling 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Boolean requireSlotFilling;
    /**
     * slots 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Map<String, String> slots;
    /**
     * parameterMapping 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Map<String, String> parameterMapping;
    /**
     * slotDefaults 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Map<String, String> slotDefaults;
    /**
     * 启用开关，表示当前对象中的对应属性。
     */
    private Boolean enabled;

    /** 状态（依据业务灵活定义） */
    private Integer status;
}
