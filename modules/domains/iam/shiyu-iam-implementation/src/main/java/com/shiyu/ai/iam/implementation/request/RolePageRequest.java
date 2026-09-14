package com.shiyu.ai.iam.implementation.request;

import com.shiyu.ai.common.core.api.PageQuery;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 角色分页查询请求 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色分页查询请求")
public class RolePageRequest extends PageQuery {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final long serialVersionUID = 1L;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    @Schema(description = "角色名称（模糊查询）")
    private String name;
}
