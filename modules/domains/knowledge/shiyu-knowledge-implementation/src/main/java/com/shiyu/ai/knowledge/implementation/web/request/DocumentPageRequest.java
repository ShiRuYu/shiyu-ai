package com.shiyu.ai.knowledge.implementation.web.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code DocumentPageRequest} 表示知识模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class DocumentPageRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * keyword 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String keyword;
    /**
     * topK 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer topK = 10;
    /**
     * knowledgeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long knowledgeId;
}
