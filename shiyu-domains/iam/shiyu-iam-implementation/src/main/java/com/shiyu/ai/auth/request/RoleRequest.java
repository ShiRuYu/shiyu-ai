package com.shiyu.ai.auth.request;

import com.shiyu.ai.auth.domain.model.RoleBO;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AutoMapper(target = RoleBO.class, reverseConvertGenerate = false)
@Schema(description = "角色创建/更新请求")
@SuppressWarnings("serial")
public class RoleRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "角色编码不能为空")
    @Schema(description = "角色编码")
    private String code;

    /**
     * 角色目标归属租户。父租户管理子租户角色时由 Service 校验后使用。
     */
    @NotNull(message = "角色归属租户不能为空")
    @Schema(description = "角色归属租户ID")
    private Long tenantId;

    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "状态（0停用 1正常）")
    private String status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "菜单/权限ID列表")
    private List<Long> permissions;
}
