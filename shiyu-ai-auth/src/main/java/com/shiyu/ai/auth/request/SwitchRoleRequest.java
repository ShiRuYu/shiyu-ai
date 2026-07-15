package com.shiyu.ai.auth.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 切换角色请求
 */
@Data
public class SwitchRoleRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "角色ID不能为空")
    private Long roleId;
}
