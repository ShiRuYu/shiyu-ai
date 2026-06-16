package com.shiyu.ai.agent.biz.auth.service;

import com.shiyu.ai.agent.domain.vo.LoginResponseVO;
import com.shiyu.ai.agent.domain.vo.WorkspaceContextVO;

import java.util.List;
import java.util.Map;

/**
 * 认证服务
 * 提供用户登录、登出等认证功能
 */
public interface AuthService {
    
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录响应（包含用户信息和访问令牌）
     */
    LoginResponseVO login(String username, String password);

    /**
     * 用户登录（带角色选择）
     * @param username 用户名
     * @param password 密码
     * @param roleId 当前角色ID（不传则默认使用第一个角色）
     * @return 登录响应（包含用户信息和访问令牌）
     */
    LoginResponseVO login(String username, String password, Long roleId);
    
    /**
     * 获取用户权限码（通过用户名）
     * @param username 用户名
     * @return 权限码列表
     */
    List<String> getAuthCodes(String username);
    
    /**
     * 获取用户权限码（通过用户 ID）
     * @param userId 用户 ID
     * @return 权限码列表
     */
    List<String> getAuthCodesByUserId(Long userId);
    
    /**
     * 刷新访问令牌
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    String refreshToken(String refreshToken);
    
    /**
     * 用户登出
     * @param refreshToken 刷新令牌
     */
    void logout(String refreshToken);

    /**
     * 切换当前角色
     * @param userId 用户 ID
     * @param roleId 目标角色 ID
     * @return 是否成功
     */
    boolean switchCurrentRole(Long userId, Long roleId);

    /**
     * 切换当前租户
     */
    boolean switchCurrentTenant(Long userId, Long tenantId);

    /**
     * 切换当前工作空间
     */
    boolean switchCurrentWorkspace(Long userId, Long workspaceId);

    /**
     * 获取用户当前租户下的工作空间列表
     */
    List<WorkspaceContextVO> getUserWorkspaces(Long userId);

    /**
     * 获取用户所有租户列表
     */
    List<Map<String, Object>> getUserTenants(Long userId);
}
