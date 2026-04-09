package com.shiyu.ai.agent.dal.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 权限码数据对象
 */
@Data
@Table(value = "auth_code")
public class AuthCodeDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
