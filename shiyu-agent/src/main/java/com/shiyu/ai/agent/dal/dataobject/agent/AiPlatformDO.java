package com.shiyu.ai.agent.dal.dataobject.agent;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * AI 平台数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "ai_platform")
public class AiPlatformDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 平台 ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 平台名称（如：OpenAI、DeepSeek）
     */
    private String name;

    /**
     * 平台编码（如：OPENAI, DEEPSEEK, OLLAMA, OPENROUTER, SILICON_FLOW）
     */
    private String code;

    /**
     * 工作空间ID
     */
    private Long workspaceId;

    /**
     * Base URL
     */
    private String baseUrl;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 默认温度参数
     */
    private Double temperature;

    /**
     * 默认最大 Token 数
     */
    private Integer maxTokens;

    /**
     * 默认最大重试次数
     */
    private Integer maxRetries;

    /**
     * 可用模型列表（JSON 数组，如 ["gpt-4o","gpt-4o-mini"]）
     */
    private String availableModels;

    /**
     * 扩展配置（JSON 对象，用于 Agent 数据源等扩展信息）
     */
    private String extraConfig;

    /**
     * 是否默认平台（Y/N）
     */
    private String isDefault;

    /**
     * 状态（1正常 0停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标志（0存在 1删除）
     */
    private String delFlag;
}
