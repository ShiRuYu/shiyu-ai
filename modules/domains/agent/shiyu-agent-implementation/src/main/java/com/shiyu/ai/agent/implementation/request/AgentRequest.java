package com.shiyu.ai.agent.implementation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Data;

/**
 * {@code AgentRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class AgentRequest {

    /**
     * agentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "Agent标识不能为空")
    @Pattern(regexp = "^[a-z][a-z0-9-]*$", message = "Agent标识只能包含小写字母、数字和连字符，以字母开头")
    private String agentId;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    @NotBlank(message = "Agent名称不能为空")
    private String name;

    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;

    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
}
