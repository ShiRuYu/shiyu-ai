package com.shiyu.ai.agent.implementation.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code ModelPageRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class ModelPageRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * platformId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long platformId;
    /**
     * pageNo 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer pageNo = 1;
    /**
     * pageSize 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer pageSize = 10;
}
