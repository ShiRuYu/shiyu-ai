package com.shiyu.ai.auth.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    @JsonIgnore
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

    /**
     * 令牌类型（默认 Bearer）
     */
    private String tokenType = "Bearer";

    /**
     * 过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 当前租户ID
     */
    private Long tenantId;

    /**
     * 当前租户名称
     */
    private String tenantName;

    /**
     * 可用租户列表
     */
    private List<Map<String, Object>> tenants;

    /**
     * 用户所属空间及角色列表
     */
    private List<WorkspaceContextVO> workspaces;
}
