package com.shiyu.ai.agent.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 登录响应数据 - 符合 API 文档规范
 */
@Data
public class LoginResponseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private Long id;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 用户名
     */
    private String username;

    /**
     * 首页路径（可选）
     */
    private String homePath;

    /**
     * 访问令牌
     */
    private String accessToken;
}
