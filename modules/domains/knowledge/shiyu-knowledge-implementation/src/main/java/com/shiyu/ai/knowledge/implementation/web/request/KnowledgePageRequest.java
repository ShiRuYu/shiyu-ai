package com.shiyu.ai.knowledge.implementation.web.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 封装 知识 Page 操作所需的请求条件和输入数据。
 */
@Data
public class KnowledgePageRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * keyword 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String keyword;
    /**
     * 学科编码，表示当前对象中的对应属性。
     */
    private String subjectCode;
    /**
     * pageNo 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer pageNo = 1;
    /**
     * pageSize 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer pageSize = 10;
}
