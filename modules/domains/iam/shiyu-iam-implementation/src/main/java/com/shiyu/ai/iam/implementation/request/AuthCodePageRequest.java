package com.shiyu.ai.iam.implementation.request;

import com.shiyu.ai.common.foundation.api.PageQuery;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 封装 认证 Code Page 操作所需的请求条件和输入数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthCodePageRequest extends PageQuery {
    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final long serialVersionUID = 1L;
    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
}
