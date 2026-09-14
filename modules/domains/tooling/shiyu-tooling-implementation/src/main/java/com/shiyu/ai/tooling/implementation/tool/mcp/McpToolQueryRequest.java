package com.shiyu.ai.tooling.implementation.tool.mcp;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code McpToolQueryRequest} 表示工具模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class McpToolQueryRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * category 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String category;
    /**
     * tag 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String tag;
    /**
     * keyword 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String keyword;
}
