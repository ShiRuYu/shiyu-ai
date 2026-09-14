package com.shiyu.ai.agent.implementation.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * {@code ExecuteRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
 */
@Data
@SuppressWarnings("serial")
public class ExecuteRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * agentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "Agent ID 不能为空")
    private String agentId;

    /**
     * 输入，表示当前对象中的对应属性。
     */
    private Map<String, Object> input;
}
