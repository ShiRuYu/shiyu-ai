package com.shiyu.ai.iam.implementation.service;

import com.shiyu.ai.iam.implementation.vo.LoginResponseVO;
import com.shiyu.ai.iam.implementation.vo.TenantInfoVO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.util.List;

/**
 * 提供 认证 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface AuthService {

    /**
     * 执行 认证 相关业务数据，并返回处理结果。
     *
     * @param username 用于完成本次业务处理的 username 参数。
     * @param password 用于完成本次业务处理的 password 参数。
     * @return 返回 认证 相关操作生成的结果数据。
     */
    LoginResponseVO login(String username, String password);

    /**
     * 执行 认证 相关业务数据，并返回处理结果。
     *
     * @param username 用于完成本次业务处理的 username 参数。
     * @param password 用于完成本次业务处理的 password 参数。
     * @param roleId 用于定位role的标识。
     * @return 返回 认证 相关操作生成的结果数据。
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
     * 查询 认证 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param username 用于完成本次业务处理的 username 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 执行 认证 相关业务数据，并返回处理结果。
     *
     * @param refreshToken 用于完成本次业务处理的 refreshToken 参数。
     * @return 返回 认证 相关操作生成的结果数据。
     */
    String refreshToken(String refreshToken);

    /**
     * 执行 认证 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param refreshToken 用于完成本次业务处理的 refreshToken 参数。
     */
    void logout(String refreshToken);

    /**
     * 执行 认证 相关业务数据，并返回处理结果。
     *
     * @param userId 当前操作涉及的用户标识。
     * @param roleId 用于定位role的标识。
     * @return 返回本次条件判断是否成立。
     */
    boolean switchCurrentRole(Long userId, Long roleId);

    /**
     * 执行 认证 相关业务数据，并返回处理结果。
     *
     * @param userId 当前操作涉及的用户标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回本次条件判断是否成立。
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
     * 创建或保存 认证 相关业务数据，并返回处理结果。
     *
     * @param username 用于完成本次业务处理的 username 参数。
     * @param password 用于完成本次业务处理的 password 参数。
     * @param email 用于完成本次业务处理的 email 参数。
     * @return 返回 认证 相关操作生成的结果数据。
     */
    LoginResponseVO register(String username, String password, String email);

    /**
     * 执行 认证 相关业务数据，并返回处理结果。
     *
     * @param phone 用于完成本次业务处理的 phone 参数。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param captchaKey 用于完成本次业务处理的 captchaKey 参数。
     * @return 返回 认证 相关操作生成的结果数据。
     */
    LoginResponseVO codeLogin(String phone, String code, String captchaKey);

    /**
     * 执行 认证 相关业务数据，并返回处理结果。
     *
     * @param email 用于完成本次业务处理的 email 参数。
     * @param newPassword 用于完成本次业务处理的 newPassword 参数。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param captchaKey 用于完成本次业务处理的 captchaKey 参数。
     * @return 返回本次条件判断是否成立。
     */
    boolean forgetPassword(String email, String newPassword, String code, String captchaKey);
}
