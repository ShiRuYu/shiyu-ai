package com.shiyu.ai.knowledge.implementation.web.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code KnowledgeSearchRequest} 表示知识模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class KnowledgeSearchRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * query 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "搜索关键词不能为空")
    private String query;

    /**
     * 学科编码，表示当前对象中的对应属性。
     */
    private String subjectCode;
    /**
     * topK 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer topK = 10;
}
