package com.shiyu.ai.iam.implementation.api.response;

import com.shiyu.ai.iam.implementation.domain.model.RoleBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;

/**
 * {@code AuthRoleResponse} 表示平台模块的响应数据，承载返回给调用方的结果。
 */
@Data
@AutoMapper(target = RoleBO.class)
public class AuthRoleResponse {
    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;
    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    private Long tenantId;
    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
    /**
     * delFlag 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer delFlag;
}
