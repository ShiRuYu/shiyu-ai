package com.shiyu.ai.governance.implementation.usage.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code UsageQueryRequest} 表示治理模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class UsageQueryRequest implements Serializable {
    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 天数，表示当前对象中的对应属性。
     */
    private int days = 7;
    /**
     * weeks 属性，保存当前对象中的业务数据或协作依赖。
     */
    private int weeks = 4;
    /**
     * months 属性，保存当前对象中的业务数据或协作依赖。
     */
    private int months = 6;
}
