package com.shiyu.ai.dal.auth.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.auth.dataobject.RoleDO;

/**
 * 角色业务对象
 */
@AutoMapper(target = RoleDO.class, reverseConvertGenerate = true)
@Data
public class RoleBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色 ID
     */
    private Long id;

    private Long tenantId;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 状态（1正常 0停用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

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

    /**
     * 权限菜单ID列表
     */
    private List<Long> permissions;
}
