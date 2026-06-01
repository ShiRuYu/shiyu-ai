package com.shiyu.ai.agent.dal.dataobject.auth;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限码数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "auth_code")
public class AuthCodeDO extends BaseEntity {

    /**
     * 权限码 ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 权限编码
     */
    private String code;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 状态（1正常 0停用）
     */
    private String status;

    /**
     * 删除标志（0：正常 1：已删除）
     */
    private Integer delFlag;
}
