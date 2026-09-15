package com.shiyu.ai.iam.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/** AUTH_TENANT_MODULE 租户模块授权记录。 */
@Data
@Table("auth_tenant_module")
public class TenantModuleAccessDO {

    /** 当前记录所属租户。 */
    @Column(tenantId = true)
    private Long tenantId;

    /** 业务模块规范标识。 */
    private String moduleId;

    /** 启用状态（1 启用，0 关闭）。 */
    private Integer status;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
