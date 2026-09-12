package com.shiyu.ai.iam.implementation.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 字典视图对象 */
@Data
public class DictVO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;
    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    private Long tenantId;
    /**
     * dictType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String dictType;
    /**
     * dictLabel 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String dictLabel;
    /**
     * dictValue 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String dictValue;
    /**
     * dictSort 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer dictSort;
    /**
     * cssClass 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String cssClass;
    /**
     * listClass 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String listClass;
    /**
     * isDefault 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String isDefault;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private String status;
    /**
     * remark 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String remark;
    /**
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;
    /**
     * updateTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime updateTime;
}
