package com.shiyu.ai.agent.implementation.request;

import com.shiyu.ai.agent.implementation.domain.model.IntentDefBO;

import io.github.linpeilie.annotations.AutoMapper;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

/**
 * {@code IntentDefRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
 */
@Data
@AutoMapper(target = IntentDefBO.class, reverseConvertGenerate = false)
public class IntentDefRequest {
    /**
     * agentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "Agent标识不能为空")
    private String agentId;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    @NotBlank(message = "意图代码不能为空")
    private String code;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    @NotBlank(message = "意图名称不能为空")
    private String name;

    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;
    /**
     * category 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String category;
    /**
     * priority 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer priority;
    /**
     * confidenceThreshold 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double confidenceThreshold;
    /**
     * targetNode 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String targetNode;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
}
