package com.shiyu.ai.iam.implementation.service;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.iam.implementation.api.request.AuthCodeRequest;
import com.shiyu.ai.iam.implementation.api.response.AuthCodeResponse;
import com.shiyu.ai.iam.implementation.request.AuthCodePageRequest;
import com.shiyu.ai.iam.implementation.vo.AuthCodeOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 提供 认证 Code 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 查询 认证 Code 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param roleId 用于定位role的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<String> listRoleAuthCodes(ActorContext actor, Long roleId, TenantId tenantId);

    /**
     * 查询 认证 Code 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<AuthCodeOptionVO> options(ActorContext actor);

    /**
     * 创建或保存 认证 Code 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 认证 Code 相关操作生成的结果数据。
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
     * 执行 认证 Code 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param roleId 用于定位role的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param authCodeIds 待处理的业务对象标识集合。
     * @return 返回本次条件判断是否成立。
     */
    boolean grant(ActorContext actor, Long roleId, TenantId tenantId, List<Long> authCodeIds);

    /**
     * 执行 认证 Code 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param roleId 用于定位role的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param authCodes 用于完成本次业务处理的 authCodes 参数。
     * @return 返回本次条件判断是否成立。
     */
    boolean replace(ActorContext actor, Long roleId, TenantId tenantId, List<String> authCodes);

    /**
     * 执行 认证 Code 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param roleId 用于定位role的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param authCodeId 用于定位auth Code的标识。
     * @return 返回本次条件判断是否成立。
     */
    boolean revoke(ActorContext actor, Long roleId, TenantId tenantId, Long authCodeId);

    /**
     * 查询 认证 Code 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 认证 Code 相关操作生成的结果数据。
     */
    PageData<AuthCodeOptionVO> page(ActorContext actor, AuthCodePageRequest request);
}
