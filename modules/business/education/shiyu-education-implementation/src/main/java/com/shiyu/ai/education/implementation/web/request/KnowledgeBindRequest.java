package com.shiyu.ai.education.implementation.web.request;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code KnowledgeBindRequest} 表示教育模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class KnowledgeBindRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 章节标识，表示当前对象中的对应属性。
     */
    @NotNull(message = "章节ID不能为空")
    private Long chapterId;

    /**
     * knowledgeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotNull(message = "知识点ID不能为空")
    private Long knowledgeId;
}
