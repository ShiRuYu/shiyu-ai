package com.shiyu.ai.agent.implementation.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

/**
 * {@code VersionRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class VersionRequest {

    /**
     * versionNumber 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "版本号不能为空")
    private String versionNumber;

    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;

    /**
     * copyFromVersionId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long copyFromVersionId;
}
