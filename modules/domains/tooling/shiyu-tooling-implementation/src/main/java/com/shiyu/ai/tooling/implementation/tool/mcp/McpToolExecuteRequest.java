package com.shiyu.ai.tooling.implementation.tool.mcp;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * {@code McpToolExecuteRequest} 表示工具模块的请求参数，承载调用方提交的输入数据。
 */
@Data
@SuppressWarnings("serial")
public class McpToolExecuteRequest implements Serializable {
    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    @NotBlank(message = "工具名称不能为空")
    private String name;

    /**
     * params 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Map<String, Object> params;
}
