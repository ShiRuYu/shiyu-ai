package com.shiyu.ai.auth.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户信息业务对象
 */
@Data
@SuppressWarnings("serial")
public class UserBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 状态（1正常 0停用）
     */
    private Integer status;

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
     * 昵称
     */
    private String nickName;

    /**
     * 性别
     */
    private String gender;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 地址
     */
    private String address;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 备注
     */
    private String remark;

    /**
     * 角色列表
     */
    private List<RoleBO> roles;

    /**
     * 当前租户作用域下已分配的角色 ID（仅用于详情返回，不映射数据库字段）。
     */
    private List<Long> roleIds;

    /**
     * 当前角色
     */
    private RoleBO currentRole;

    /**
     * 扩展信息（JSON格式）
     */
    private String extInfo;
}
