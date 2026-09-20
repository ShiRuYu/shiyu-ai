package com.shiyu.ai.governance.implementation.usage.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 封装 用量 Overview 操作向调用方返回的传输数据。
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
