package com.shiyu.ai.iam.implementation.service;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.iam.implementation.api.request.AuthCodeRequest;
import com.shiyu.ai.iam.implementation.api.response.AuthCodeResponse;
import com.shiyu.ai.iam.implementation.request.AuthCodePageRequest;
import com.shiyu.ai.iam.implementation.vo.AuthCodeOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * AuthCodeService 服务接口，负责执行身份与访问领域相关业务操作。
 */
public interface AuthCodeService {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     *
     * @return 符合条件的结果集合。
     */
    List<AuthCodeOptionVO> list(ActorContext actor);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param roleId 方法参数。
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<String> listRoleAuthCodes(ActorContext actor, Long roleId, TenantId tenantId);

    /**
     * 执行 {@code options} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     *
     * @return 符合条件的结果集合。
     */
    List<AuthCodeOptionVO> options(ActorContext actor);

    /**
     * 创建并保存业务对象。
     *
     * @param actor 当前操作主体上下文。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    AuthCodeResponse create(ActorContext actor, AuthCodeRequest request);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     * @param request 请求参数。
     *
     * @return 条件是否满足。
     */
    boolean update(ActorContext actor, Long id, AuthCodeRequest request);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     *
     * @return 条件是否满足。
     */
    boolean delete(ActorContext actor, Long id);

    /**
     * 执行 {@code grant} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param roleId 方法参数。
     * @param tenantId 租户标识。
     * @param authCodeIds 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean grant(ActorContext actor, Long roleId, TenantId tenantId, List<Long> authCodeIds);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param roleId 方法参数。
     * @param tenantId 租户标识。
     * @param authCodes 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean replace(ActorContext actor, Long roleId, TenantId tenantId, List<String> authCodes);

    /**
     * 执行 {@code revoke} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param roleId 方法参数。
     * @param tenantId 租户标识。
     * @param authCodeId 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean revoke(ActorContext actor, Long roleId, TenantId tenantId, Long authCodeId);

    /**
     * 执行 {@code page} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    PageData<AuthCodeOptionVO> page(ActorContext actor, AuthCodePageRequest request);
}
