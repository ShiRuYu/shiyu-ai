package com.shiyu.ai.auth.request;

import com.shiyu.ai.common.core.api.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色分页查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色分页查询请求")
public class RolePageRequest extends PageQuery {

    private static final long serialVersionUID = 1L;

    @Schema(description = "角色名称（模糊查询）")
    private String name;

}
