package com.shiyu.ai.agent.domain.bo;

import com.shiyu.ai.dal.dataobject.agent.AiModelDO;
import com.shiyu.ai.common.core.domain.BaseEntity;
import com.shiyu.ai.common.core.validate.AddGroup;
import com.shiyu.ai.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * AI 模型业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AiModelDO.class, reverseConvertGenerate = true)
public class AiModelBO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模型 ID
     */
    @NotNull(message = "模型ID不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 所属平台 ID
     */
    @NotNull(message = "平台ID不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long platformId;

    /**
     * 模型名称
     */
    @NotBlank(message = "模型名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 100, message = "模型名称长度不能超过{max}个字符")
    private String modelName;

    /**
     * 模型显示名称
     */
    @Size(max = 100, message = "模型显示名称长度不能超过{max}个字符")
    private String displayName;

    /**
     * 模型描述
     */
    @Size(max = 500, message = "模型描述长度不能超过{max}个字符")
    private String description;

    /**
     * 模型级参数覆盖（JSON）
     */
    private String modelConfig;

    /**
     * 是否默认模型（Y/N）
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
     * 平台名
     */
    private String platformName;
}
