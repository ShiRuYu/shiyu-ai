package com.shiyu.ai.agent.dal.dataobject.auth;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 认证码数据对象
 */
@Data
@Table(value = "auth_code")
public class AuthCodeDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 验证码
     */
    private String code;

    /**
     * 手机号/邮箱
     */
    private String target;

    /**
     * 类型（login/register/reset）
     */
    private String type;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 是否使用
     */
    private Integer used;

    /**
     * 创建时间
     */
    private Date createTime;
}
