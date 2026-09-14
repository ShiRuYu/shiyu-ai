package com.shiyu.ai.iam.implementation.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** 权限码视图对象 */
@Data
@SuppressWarnings("serial")
public class AuthCodeVO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * codes 属性，保存当前对象中的业务数据或协作依赖。
     */
    private List<String> codes;
}
