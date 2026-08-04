package com.shiyu.ai.dal.auth.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.common.mybatis.model.ServiceAssignedTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;
import com.shiyu.ai.auth.domain.model.RoleBO;
import io.github.linpeilie.annotations.AutoMapper;

/**
 * 角色数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "auth_role")
@AutoMapper(target = RoleBO.class, reverseConvertGenerate = true)
public class RoleDO extends TenantEntity implements ServiceAssignedTenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 备注
     */
    private String remark;

}
