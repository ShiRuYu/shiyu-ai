package com.shiyu.ai.agent.domain.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户请求对象
 */
@Data
public class UserRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 用户性别（0 男 1 女 2 未知）
     */
    private String sex;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 帐号状态（1 正常 0 停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 角色组
     */
    private Long[] roleIds;

    /**
     * 岗位组
     */
    private Long[] postIds;
}
