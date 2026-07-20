package com.shiyu.ai.dal.auth.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.auth.dataobject.UserDO;

/**
 * 用户信息业务对象
 */
@AutoMapper(target = UserDO.class, reverseConvertGenerate = true)
@Data
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
    private List<RoleBO> roles;

    /**
     * 当前角色
     */
    private RoleBO currentRole;

    /**
     * 扩展信息（JSON格式）
     */
    private String extInfo;
}
