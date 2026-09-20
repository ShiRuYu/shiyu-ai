package com.shiyu.ai.agent.implementation.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 封装 Runtime Execution 操作向调用方返回的传输数据。
 */
@Data
@SuppressWarnings("serial")
public class RuntimeExecutionVO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 执行标识，表示当前对象中的对应属性。
     */
    private String executionId;
    /**
     * agentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String agentId;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
    /**
     * 输入，表示当前对象中的对应属性。
     */
    private Map<String, Object> input;
    /**
     * 输出，表示当前对象中的对应属性。
     */
    private Map<String, Object> output;
    /**
     * 错误，表示当前对象中的对应属性。
     */
    private String error;
    /**
     * startTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long startTime;
    /**
     * endTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long endTime;
    /**
     * events 属性，保存当前对象中的业务数据或协作依赖。
     */
    private List<String> events;
}
