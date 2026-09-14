package com.shiyu.ai.iam.implementation.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 权限码下拉及授权选项。 */
@Data
public class AuthCodeOptionVO implements Serializable {

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
     * 编码，表示当前对象中的对应属性。
     */
    private String code;

    /**
     * module 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String module;

    /**
     * 资源，表示当前对象中的对应属性。
     */
    private String resource;

    /**
     * action 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String action;

    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;

    /**
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;
}
