package com.shiyu.ai.governance.implementation.usage.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code UsageOverviewVO} 承载治理模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Data
public class UsageOverviewVO implements Serializable {
    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * totalTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long totalTokens;
    /**
     * totalCalls 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long totalCalls;
    /**
     * todayTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long todayTokens;
    /**
     * todayCalls 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long todayCalls;
    /**
     * activeModels 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer activeModels;
}
