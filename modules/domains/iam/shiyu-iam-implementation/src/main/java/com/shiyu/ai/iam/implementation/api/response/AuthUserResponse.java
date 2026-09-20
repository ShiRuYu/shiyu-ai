package com.shiyu.ai.iam.implementation.api.response;

import com.shiyu.ai.iam.implementation.domain.model.UserBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;

/**
 * 封装 认证 用户 操作向调用方返回的传输数据。
 */
@Data
@AutoMapper(target = UserBO.class)
public class AuthUserResponse {
    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;
    /**
     * username 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String username;
    /**
     * nickName 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String nickName;
    /**
     * avatar 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String avatar;
    /**
     * extInfo 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String extInfo;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
    /**
     * delFlag 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer delFlag;
}
