package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.MenuBO;
import com.shiyu.ai.kernel.context.TenantId;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 负责 Menu 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface MenuRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<MenuBO> selectAll(TenantId tenantId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param type 对象类型。
     *
     * @return 符合条件的结果集合。
     */
    List<MenuBO> selectAllByType(TenantId tenantId, String type);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param type 对象类型。
     *
     * @return 符合条件的结果集合。
     */
    List<MenuBO> selectAllExcludingType(TenantId tenantId, String type);

    /**
     * 查询 Menu 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 Menu 相关操作生成的结果数据。
     */
    MenuBO selectById(TenantId tenantId, Long id);

    /**
     * 创建或保存 Menu 相关业务数据，并返回处理结果。
     *
     * @param menuBO 用于完成本次业务处理的 menuBO 参数。
     * @return 返回 Menu 相关操作生成的结果数据。
     */
    MenuBO insert(MenuBO menuBO);

    /**
     * 更新或设置 Menu 相关业务数据，并返回处理结果。
     *
     * @param menuBO 用于完成本次业务处理的 menuBO 参数。
     * @return 返回本次条件判断是否成立。
     */
    boolean update(MenuBO menuBO);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 条件是否满足。
     */
    boolean deleteById(TenantId tenantId, Long id);

    /**
     * 查询 Menu 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param pageNo 分页页码，从 1 开始。
     * @param pageSize 每页返回的数据数量。
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param type 用于完成本次业务处理的 type 参数。
     * @param status 用于完成本次业务处理的 status 参数。
     * @return 返回总数及当前页数据，左值为总数，右值为数据列表。
     */
    Pair<Long, List<MenuBO>> selectPage(
            TenantId tenantId,
            Number pageNo,
            Number pageSize,
            String name,
            String code,
            String type,
            Integer status);

    /**
     * 查询 Menu 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param userId 当前操作涉及的用户标识。
     * @param roleCode 用于完成本次业务处理的 roleCode 参数。
     * @param parentSuperAdminSwitch 用于完成本次业务处理的 parentSuperAdminSwitch 参数。
     * @param excludeType 用于完成本次业务处理的 excludeType 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<MenuBO> selectMenusByUserId(
            TenantId tenantId,
            Long userId,
            String roleCode,
            boolean parentSuperAdminSwitch,
            String excludeType);

    /**
     * 执行 Menu 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @param excludeId 用于定位exclude的标识。
     * @return 返回本次条件判断是否成立。
     */
    boolean existsByName(TenantId tenantId, String name, Long excludeId);

    /**
     * 执行 Menu 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param path 用于完成本次业务处理的 path 参数。
     * @param excludeId 用于定位exclude的标识。
     * @return 返回本次条件判断是否成立。
     */
    boolean existsByPath(TenantId tenantId, String path, Long excludeId);

    /**
     * 查询 Menu 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param parentId 用于定位parent的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<MenuBO> selectByParentId(TenantId tenantId, Long parentId);

    /**
     * 查询 Menu 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param parentId 用于定位parent的标识。
     * @param type 用于完成本次业务处理的 type 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<MenuBO> selectByParentIdAndType(TenantId tenantId, Long parentId, String type);
}
