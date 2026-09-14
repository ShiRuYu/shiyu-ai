package com.shiyu.ai.education.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** Resource 业务对象 */
@Data
public class ResourceBO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;

    /**
     * 类型，表示当前对象中的对应属性。
     */
    private String type;

    /**
     * 地址，表示当前对象中的对应属性。
     */
    private String url;

    /**
     * sizeBytes 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long sizeBytes;

    /**
     * durationSec 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer durationSec;

    /**
     * 学科编码，表示当前对象中的对应属性。
     */
    private String subjectCode;

    /**
     * grade 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer grade;

    /**
     * difficulty 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer difficulty;

    /**
     * coverUrl 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String coverUrl;

    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;

    /**
     * viewCount 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long viewCount;

    /**
     * 创建时间，表示当前对象中的对应属性。
     */
    private LocalDateTime createdAt;

    /** 状态（依据业务灵活定义） */
    private Integer status;
}
