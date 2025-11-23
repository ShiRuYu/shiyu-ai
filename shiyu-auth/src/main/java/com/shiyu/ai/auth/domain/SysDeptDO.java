package com.shiyu.ai.auth.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.core.domain.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 部门表 sys_dept
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("sys_dept")
public class SysDeptDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 部门ID
     */
    @Id
    private Long deptId;

    /**
     * 父部门ID
     */
    private Long parentId;

    /**
     * 部门名称
     */
    private String deptName;

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
     * 部门状态:1正常,0停用
     */
    private String status;

    /**
     * 删除标志（1代表存在 0代表删除）
     */
    private String delFlag;

    /**
     * 祖级列表
     */
    private String ancestors;

}
