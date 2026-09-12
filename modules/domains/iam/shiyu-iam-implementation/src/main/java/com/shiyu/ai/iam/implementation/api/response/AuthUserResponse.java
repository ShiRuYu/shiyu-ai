package com.shiyu.ai.iam.implementation.api.response;

import com.shiyu.ai.iam.implementation.domain.model.UserBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;

/**
 * {@code AuthUserResponse} 表示平台模块的响应数据，承载返回给调用方的结果。
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
