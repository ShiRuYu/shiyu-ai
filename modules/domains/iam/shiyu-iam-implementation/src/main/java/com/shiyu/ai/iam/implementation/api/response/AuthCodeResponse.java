package com.shiyu.ai.iam.implementation.api.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 封装 认证 Code 操作向调用方返回的传输数据。
 */
@Data
public class AuthCodeResponse {
    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;
    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
    /**
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;
    /**
     * updateTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime updateTime;
}
