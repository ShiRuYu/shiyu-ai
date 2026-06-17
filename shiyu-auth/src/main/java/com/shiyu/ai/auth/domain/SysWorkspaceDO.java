package com.shiyu.ai.auth.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 工作空间表 sys_workspace
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("sys_workspace")
public class SysWorkspaceDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 工作空间ID
     */
    @Id
    private Long workspaceId;

    /**
     * 父工作空间ID
     */
    private Long parentId;

    /**
     * 工作空间名称
     */
    private String workspaceName;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 工作空间状态:1正常,0停用
     */
    private String status;

    /**
     * 删除标志（0 代表存在 1 代表删除）
     */
    private String delFlag;

    /**
     * 祖级列表
     */
    private String ancestors;

}
