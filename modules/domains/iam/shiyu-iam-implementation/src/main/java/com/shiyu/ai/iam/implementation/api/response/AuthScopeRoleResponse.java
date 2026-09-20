package com.shiyu.ai.iam.implementation.api.response;

import com.shiyu.ai.iam.implementation.domain.model.UserScopeRoleBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;

/**
 * 封装 认证 Scope 角色 操作向调用方返回的传输数据。
 */
@Data
@AutoMapper(target = UserScopeRoleBO.class)
public class AuthScopeRoleResponse {
    /**
     * 用户标识，表示当前对象中的对应属性。
     */
    private Long userId;
    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    private Long tenantId;
    /**
     * 角色标识，表示当前对象中的对应属性。
     */
    private Long roleId;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
    /**
     * delFlag 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer delFlag;
}
