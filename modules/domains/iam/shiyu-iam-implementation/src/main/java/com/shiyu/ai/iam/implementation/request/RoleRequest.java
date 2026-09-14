package com.shiyu.ai.iam.implementation.request;

import com.shiyu.ai.iam.implementation.domain.model.RoleBO;

import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * {@code RoleRequest} 表示平台模块的请求参数，承载调用方提交的输入数据。
 */
@Data
@AutoMapper(target = RoleBO.class, reverseConvertGenerate = false)
@Schema(description = "角色创建/更新请求")
@SuppressWarnings("serial")
public class RoleRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    @NotBlank(message = "角色编码不能为空")
    @Schema(description = "角色编码")
    private String code;

    /** 角色目标归属租户。父租户管理子租户角色时由 Service 校验后使用。 */
    @NotNull(message = "角色归属租户不能为空")
    @Schema(description = "角色归属租户ID")
    private Long tenantId;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名称")
    private String name;

    /**
     * 状态，表示当前对象中的对应属性。
     */
    @Schema(description = "状态（0停用 1正常）")
    private String status;

    /**
     * remark 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * permissions 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "菜单/权限ID列表")
    private List<Long> permissions;
}
