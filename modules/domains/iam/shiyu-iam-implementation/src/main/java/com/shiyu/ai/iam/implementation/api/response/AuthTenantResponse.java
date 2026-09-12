package com.shiyu.ai.iam.implementation.api.response;

import com.shiyu.ai.iam.implementation.domain.model.TenantBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;

/**
 * {@code AuthTenantResponse} 表示平台模块的响应数据，承载返回给调用方的结果。
 */
@Data
@AutoMapper(target = TenantBO.class)
public class AuthTenantResponse {
    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
    /**
     * delFlag 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer delFlag;
}
