package com.shiyu.ai.agent.implementation.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 封装 Intent Page 操作所需的请求条件和输入数据。
 */
@Data
public class IntentPageRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * agentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String agentId;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;
    /**
     * category 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String category;
    /**
     * pageNo 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer pageNo = 1;
    /**
     * pageSize 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer pageSize = 10;
}
