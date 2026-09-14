package com.shiyu.ai.iam.implementation.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 用户租户分配信息。 */
@Data
public class UserTenantAssignmentVO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    private Long tenantId;
    /**
     * 租户名称，表示当前对象中的对应属性。
     */
    private String tenantName;
    /**
     * 角色标识，表示当前对象中的对应属性。
     */
    private Long roleId;
    /**
     * 角色名称，表示当前对象中的对应属性。
     */
    private String roleName;
    /**
     * 角色编码，表示当前对象中的对应属性。
     */
    private String roleCode;
}
