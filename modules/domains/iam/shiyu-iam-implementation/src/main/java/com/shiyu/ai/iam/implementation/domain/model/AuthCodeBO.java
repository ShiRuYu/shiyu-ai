package com.shiyu.ai.iam.implementation.domain.model;

import com.shiyu.ai.common.core.domain.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/** 权限码数据对象 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthCodeBO extends BaseEntity {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /** 权限码 ID */
    private Long id;

    /** 权限编码 */
    private String code;

    /** 权限名称 */
    private String name;
}
