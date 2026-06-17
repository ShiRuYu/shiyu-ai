package com.shiyu.ai.agent.dal.dataobject.agent;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * AI 模型数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "ai_model")
public class AiModelDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模型 ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 所属平台 ID
     */
    private Long platformId;

    /**
     * 模型名称（如：gpt-4o, deepseek-chat）
     */
    private String modelName;

    /**
     * 工作空间ID
     */
    private Long workspaceId;

    /**
     * 模型显示名称
     */
    private String displayName;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 模型级参数覆盖（JSON 对象，如 {"temperature":0.5,"maxTokens":8192}）
     */
    private String modelConfig;

    /**
     * 是否默认模型（Y/N，每个平台只有一个默认）
     */
    private String isDefault;

    /**
     * 状态（1正常 0停用）
     */
    private String status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 删除标志（0存在 1删除）
     */
    private String delFlag;
}
