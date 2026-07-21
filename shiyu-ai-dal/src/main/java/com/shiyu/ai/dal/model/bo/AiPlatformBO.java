package com.shiyu.ai.dal.model.bo;

import com.shiyu.ai.common.core.domain.BaseEntity;
import com.shiyu.ai.common.core.validate.AddGroup;
import com.shiyu.ai.common.core.validate.EditGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.model.dataobject.AiPlatformDO;

import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * AI 平台业务对象
 */
@AutoMapper(target = AiPlatformDO.class, reverseConvertGenerate = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class AiPlatformBO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 平台 ID
     */
    @NotNull(message = "平台ID不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 平台名称
     */
    @NotBlank(message = "平台名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 50, message = "平台名称长度不能超过{max}个字符")
    private String name;

    /**
     * 平台编码
     */
    @NotBlank(message = "平台编码不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 50, message = "平台编码长度不能超过{max}个字符")
    private String code;

    /**
     * Base URL
     */
    @Size(max = 500, message = "Base URL 长度不能超过{max}个字符")
    private String baseUrl;

    /**
     * API Key
     */
    @Size(max = 500, message = "API Key 长度不能超过{max}个字符")
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
     * 可用模型列表（JSON 数组）
     */
    private String availableModels;

    /**
     * 扩展配置（JSON 对象，Agent 数据源等）
     */
    private String extraConfig;

    /**
     * 是否默认平台（Y/N）
     */
    private String isDefault;

    /**
     * 备注
     */
    private String remark;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;

}
