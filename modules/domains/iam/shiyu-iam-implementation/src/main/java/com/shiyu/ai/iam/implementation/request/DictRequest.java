package com.shiyu.ai.iam.implementation.request;

import lombok.Data;

/**
 * {@code DictRequest} 表示平台模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class DictRequest {
    /**
     * dictType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String dictType;
    /**
     * dictLabel 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String dictLabel;
    /**
     * dictValue 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String dictValue;
    /**
     * dictSort 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer dictSort;
    /**
     * cssClass 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String cssClass;
    /**
     * listClass 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String listClass;
    /**
     * isDefault 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String isDefault;
    /**
     * remark 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String remark;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
}
