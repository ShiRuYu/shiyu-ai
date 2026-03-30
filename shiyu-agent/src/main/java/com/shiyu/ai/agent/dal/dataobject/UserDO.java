package com.shiyu.ai.agent.dal.dataobject;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户数据对象（模拟数据）
 */
@Data
public class UserDO implements Serializable {

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
     * 是否启用
     */
    private Boolean enable;

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
     * 角色列表
     */
    private List<RoleDO> roles;

    /**
     * 当前角色
     */
    private RoleDO currentRole;
}
