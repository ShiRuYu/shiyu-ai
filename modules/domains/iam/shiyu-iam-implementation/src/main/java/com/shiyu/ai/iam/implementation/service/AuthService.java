package com.shiyu.ai.iam.implementation.service;

import com.shiyu.ai.iam.implementation.vo.LoginResponseVO;
import com.shiyu.ai.iam.implementation.vo.TenantInfoVO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.util.List;

/** 认证服务 提供用户登录、登出等认证功能 */
public interface AuthService {

    /**
     * 执行 {@code login} 定义的接口操作。
     *
     * @param username 方法参数。
     * @param password 方法参数。
     *
     * @return 操作结果。
     */
    LoginResponseVO login(String username, String password);

    /**
     * 执行 {@code login} 定义的接口操作。
     *
     * @param username 方法参数。
     * @param password 方法参数。
     * @param roleId 方法参数。
     *
     * @return 操作结果。
     */
    LoginResponseVO login(String username, String password, Long roleId);

    /**
     * 处理登录。
     *
     * @param username username 参数。
     * @param password password 参数。
     * @param roleId roleId 参数。
     * @param loginIp loginIp 参数。
     *
     * @return 处理结果。
     */
    LoginResponseVO login(String username, String password, Long roleId, String loginIp);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param username 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<String> getAuthCodes(ActorContext actor, String username);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param userId 用户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<String> getAuthCodesByUserId(ActorContext actor, UserId userId);

    /**
     * 执行 {@code refreshToken} 定义的接口操作。
     *
     * @param refreshToken 方法参数。
     *
     * @return 操作结果。
     */
    String refreshToken(String refreshToken);

    /**
     * 执行 {@code logout} 定义的接口操作。
     *
     * @param refreshToken 方法参数。
     */
    void logout(String refreshToken);

    /**
     * 执行 {@code switchCurrentRole} 定义的接口操作。
     *
     * @param userId 用户标识。
     * @param roleId 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean switchCurrentRole(Long userId, Long roleId);

    /**
     * 执行 {@code switchCurrentTenant} 定义的接口操作。
     *
     * @param userId 用户标识。
     * @param tenantId 租户标识。
     *
     * @return 条件是否满足。
     */
    boolean switchCurrentTenant(Long userId, TenantId tenantId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param userId 用户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<TenantInfoVO> getUserTenants(ActorContext actor, Long userId);

    /**
     * 创建并保存业务对象。
     *
     * @param username 方法参数。
     * @param password 方法参数。
     * @param email 方法参数。
     *
     * @return 操作结果。
     */
    LoginResponseVO register(String username, String password, String email);

    /**
     * 执行 {@code codeLogin} 定义的接口操作。
     *
     * @param phone 方法参数。
     * @param code 方法参数。
     * @param captchaKey 方法参数。
     *
     * @return 操作结果。
     */
    LoginResponseVO codeLogin(String phone, String code, String captchaKey);

    /**
     * 执行 {@code forgetPassword} 定义的接口操作。
     *
     * @param email 方法参数。
     * @param newPassword 方法参数。
     * @param code 方法参数。
     * @param captchaKey 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean forgetPassword(String email, String newPassword, String code, String captchaKey);
}
