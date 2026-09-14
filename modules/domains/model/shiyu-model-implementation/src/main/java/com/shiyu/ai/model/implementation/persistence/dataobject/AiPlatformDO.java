package com.shiyu.ai.model.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.model.implementation.domain.model.AiPlatformBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/** AI 平台数据对象 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "model_ai_platform")
@AutoMapper(target = AiPlatformBO.class, reverseConvertGenerate = true)
public class AiPlatformDO extends TenantEntity {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /** 平台 ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 平台名称（如：OpenAI、DeepSeek） */
    private String name;

    /** 平台编码（如：OPENAI, DEEPSEEK, OLLAMA, OPENROUTER, SILICON_FLOW） */
    private String code;

    /** 模型平台 API 地址。 */
    private String baseUrl;

    /** 模型调用的默认温度参数。 */
    private Double temperature;

    /** 默认最大 Token 数 */
    private Integer maxTokens;

    /** 默认最大重试次数 */
    private Integer maxRetries;

    /** 可用模型列表（JSON 数组，如 ["gpt-4o","gpt-4o-mini"]） */
    private String availableModels;

    /** 扩展配置（JSON 对象，用于 Agent 数据源等扩展信息） */
    private String extraConfig;

    /** 是否默认平台（Y/N） */
    private String isDefault;

    /** 备注 */
    private String remark;
}
