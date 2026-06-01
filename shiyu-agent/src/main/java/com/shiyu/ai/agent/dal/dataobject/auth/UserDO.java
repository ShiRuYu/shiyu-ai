package com.shiyu.ai.agent.dal.dataobject.auth;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "user")
public class UserDO extends BaseEntity {

    /**
     * 用户ID
     */
    @Id(keyType = KeyType.Auto)
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
}
