package com.shiyu.ai.iam.implementation.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 租户信息视图对象（替代 Map<String, Object>） */
@Data
public class TenantInfoVO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;
    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;

    /** 租户树展示路径，例如：A / A1 / A1-1。 */
    private String pathName;
}
