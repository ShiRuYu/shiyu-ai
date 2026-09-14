package com.shiyu.ai.governance.implementation.usage.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code UsageAggregateVO} 承载治理模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Data
public class UsageAggregateVO implements Serializable {
    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * period 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String period;
    /**
     * totalTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long totalTokens;
    /**
     * totalCalls 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long totalCalls;
    /**
     * modelName 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String modelName;
}
