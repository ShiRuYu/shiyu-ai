package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import com.shiyu.ai.kernel.context.TenantId;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * 负责 租户 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface TenantRepository {
    /**
     * 查询 租户 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param pageNo 分页页码，从 1 开始。
     * @param pageSize 每页返回的数据数量。
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param status 用于完成本次业务处理的 status 参数。
     * @return 返回总数及当前页数据，左值为总数，右值为数据列表。
     */
    Pair<Long, List<TenantBO>> selectPage(
            TenantId tenantId,
            Number pageNo,
            Number pageSize,
            String name,
            String code,
            Integer status);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @return 符合条件的结果集合。
     */
    List<TenantBO> selectAll();

    /**
     * 查询 租户 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 租户 相关操作生成的结果数据。
     */
    TenantBO selectById(Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 操作影响的记录数或状态码。
     */
    Long selectRootTenantId(TenantId tenantId);

    /**
     * 保存租户基础记录并返回生成的标识；安全数据由初始化入口另行创建。
     *
     * @param tenantBO 新租户的基础信息。
     * @param sourceTenantId 当前经授权创建租户的来源租户。
     *
     * @return 包含持久化标识的租户信息。
     */
    TenantBO insert(TenantBO tenantBO, TenantId sourceTenantId);

    /**
     * 为新租户创建管理员角色、管理员账号及从授权来源复制的菜单和权限。
     * 调用方必须已经绑定新租户作用域，并验证来源租户的管理权限。
     *
     * @param tenantBO 已持久化的新租户及初始化配置。
     * @param sourceTenantId 经授权提供菜单和权限模板的来源租户。
     */
    void initializeTenantSecurity(TenantBO tenantBO, TenantId sourceTenantId);

    /**
     * 更新或设置 租户 相关业务数据，并返回处理结果。
     *
     * @param tenantBO 用于完成本次业务处理的 tenantBO 参数。
     * @return 返回本次条件判断是否成立。
     */
    boolean update(TenantBO tenantBO);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param id 目标对象标识。
     *
     * @return 条件是否满足。
     */
    boolean deleteById(Long id);

    /**
     * 执行 租户 相关业务数据，并返回处理结果。
     *
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param excludeId 用于定位exclude的标识。
     * @return 返回本次条件判断是否成立。
     */
    boolean existsByCode(String code, Long excludeId);

    /**
     * 执行 租户 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     */
    void cascadeDelete(TenantId tenantId);

    /**
     * 查询 租户 相关业务数据，并返回处理结果。
     *
     * @param rootId 用于定位root的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Long> selectDescendantIds(TenantId rootId);
}
