package com.shiyu.ai.iam.implementation.request;

import com.shiyu.ai.common.foundation.api.PageQuery;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 封装 Menu Page 操作所需的请求条件和输入数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MenuPageRequest extends PageQuery {
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
     * 类型，表示当前对象中的对应属性。
     */
    private String type;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
}
