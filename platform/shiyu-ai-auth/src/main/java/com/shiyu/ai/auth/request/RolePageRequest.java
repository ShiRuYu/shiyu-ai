package com.shiyu.ai.auth.request;

import com.shiyu.ai.common.core.api.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色分页查询请求
 */
@Data
@Schema(description = "角色分页查询请求")
public class RolePageRequest extends PageQuery {

    @Schema(description = "角色名称（模糊查询）")
    private String name;

}
