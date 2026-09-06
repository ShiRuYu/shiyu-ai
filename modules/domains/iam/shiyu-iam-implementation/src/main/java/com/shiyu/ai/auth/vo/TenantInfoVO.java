package com.shiyu.ai.auth.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 租户信息视图对象（替代 Map<String, Object>）
 */
@Data
public class TenantInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String code;
    private String name;

    /** 租户树展示路径，例如：A / A1 / A1-1。 */
    private String pathName;
}
