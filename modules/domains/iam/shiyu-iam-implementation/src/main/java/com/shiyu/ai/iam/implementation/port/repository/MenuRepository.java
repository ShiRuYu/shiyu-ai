package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.MenuBO;
import com.shiyu.ai.kernel.context.TenantId;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * MenuRepository 仓储接口，负责访问和持久化身份与访问领域聚合数据。
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
     * 根据标识查询对应的数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    MenuBO selectById(TenantId tenantId, Long id);

    /**
     * 创建并保存业务对象。
     *
     * @param menuBO 方法参数。
     *
     * @return 操作结果。
     */
    MenuBO insert(MenuBO menuBO);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param menuBO 方法参数。
     *
     * @return 条件是否满足。
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
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param pageNo 方法参数。
     * @param pageSize 分页大小。
     * @param name 对象名称。
     * @param code 方法参数。
     * @param type 对象类型。
     * @param status 对象状态。
     *
     * @return 符合条件的结果集合。
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
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param userId 用户标识。
     * @param roleCode 方法参数。
     * @param parentSuperAdminSwitch 方法参数。
     * @param excludeType 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<MenuBO> selectMenusByUserId(
            TenantId tenantId,
            Long userId,
            String roleCode,
            boolean parentSuperAdminSwitch,
            String excludeType);

    /**
     * 判断当前条件是否满足。
     *
     * @param tenantId 租户标识。
     * @param name 对象名称。
     * @param excludeId 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean existsByName(TenantId tenantId, String name, Long excludeId);

    /**
     * 判断当前条件是否满足。
     *
     * @param tenantId 租户标识。
     * @param path 方法参数。
     * @param excludeId 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean existsByPath(TenantId tenantId, String path, Long excludeId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param parentId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<MenuBO> selectByParentId(TenantId tenantId, Long parentId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param parentId 方法参数。
     * @param type 对象类型。
     *
     * @return 符合条件的结果集合。
     */
    List<MenuBO> selectByParentIdAndType(TenantId tenantId, Long parentId, String type);
}
