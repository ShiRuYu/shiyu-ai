package com.shiyu.ai.auth.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录响应视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 令牌类型
     */
    private String tokenType;

    /**
     * 过期时间 (秒)
     */
    private Long expiresIn;

    /**
     * 用户信息
     */
    private SysUserVO userInfo;
}
