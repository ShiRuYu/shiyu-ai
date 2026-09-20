package com.shiyu.ai.agent.implementation.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 封装 模型 Page 操作所需的请求条件和输入数据。
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
