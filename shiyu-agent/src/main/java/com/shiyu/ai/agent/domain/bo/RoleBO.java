package com.shiyu.ai.agent.domain.bo;

import com.shiyu.ai.agent.dal.dataobject.RoleDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 角色业务对象
 */
@Data
@AutoMapper(target = RoleDO.class, reverseConvertGenerate = true)
public class RoleBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色 ID
     */
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
