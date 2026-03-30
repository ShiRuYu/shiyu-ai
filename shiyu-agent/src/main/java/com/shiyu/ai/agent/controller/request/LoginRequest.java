package com.shiyu.ai.agent.controller.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录请求对象
 */
@Data
public class LoginRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 验证码
     */
    private String captcha;

    /**
     * 验证码 key（可选，用于验证验证码）
     */
    private String captchaKey;
}
