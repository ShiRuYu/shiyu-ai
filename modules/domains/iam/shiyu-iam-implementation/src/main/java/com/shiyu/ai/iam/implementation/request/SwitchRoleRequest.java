package com.shiyu.ai.iam.implementation.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 切换角色请求 */
@Data
@Schema(description = "切换角色请求")
public class SwitchRoleRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 角色标识，表示当前对象中的对应属性。
     */
    @NotNull(message = "角色ID不能为空")
    @Schema(description = "目标角色ID")
    private Long roleId;
}
