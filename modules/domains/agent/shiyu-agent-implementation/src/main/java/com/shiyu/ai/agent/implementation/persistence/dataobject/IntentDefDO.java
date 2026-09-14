package com.shiyu.ai.agent.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * {@code IntentDefDO} 是智能体模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "agent_intent_def")
public class IntentDefDO extends TenantEntity {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
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
    private String examples; // JSON array
    /**
     * targetNode 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String targetNode;
    /**
     * requireSlotFilling 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String requireSlotFilling; // '1' or '0'
    /**
     * slots 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String slots; // JSON object
    /**
     * parameterMapping 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String parameterMapping; // JSON object
    /**
     * slotDefaults 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String slotDefaults; // JSON object
    /**
     * 启用开关，表示当前对象中的对应属性。
     */
    private String enabled; // '1' or '0'
}
