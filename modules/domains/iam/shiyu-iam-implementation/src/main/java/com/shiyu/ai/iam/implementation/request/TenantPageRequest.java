package com.shiyu.ai.iam.implementation.request;

import com.shiyu.ai.common.core.api.PageQuery;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * {@code TenantPageRequest} 表示平台模块的请求参数，承载调用方提交的输入数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantPageRequest extends PageQuery {
    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final long serialVersionUID = 1L;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
}
