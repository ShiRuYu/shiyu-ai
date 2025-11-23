package com.shiyu.ai.auth.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.core.domain.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户对象 sys_user
 */

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table("sys_user")
public class SysUserDO extends TenantEntity {

    /**
     * 用户ID
     */
    @Id
    private Long userId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户套餐
     */
    private String userPlan;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户类型（sys_user系统用户）
     */
    private String userType;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phonenumber;

    /**
     * 用户性别
     */
    private String sex;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 密码
     */
    private String password;

    /**
     * 帐号状态（1正常 0停用）
     */
    private String status;

    /**
     * 删除标志（1代表存在 0代表删除）
     */
    private String delFlag;

    /**
     * 最后登录IP
     */
    private String loginIp;

    /**
     * 注册域名
     */
    private String domainName;

    /**
     * 最后登录时间
     */
    private Date loginDate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 普通用户的标识,对当前开发者帐号唯一。一个openid对应一个公众号或小程序
     */
    private String openId;

    /**
     * 用户余额
     */
    private Double userBalance;

    /**
     * 用户等级
     */
    private String userGrade;

    public SysUserDO(Long userId) {
        this.userId = userId;
    }

    public boolean isSuperAdmin() {
        return false;
    }

}
