package com.shiyu.ai.auth.port.repository;

import com.shiyu.ai.auth.domain.model.MenuBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface MenuRepository {
    List<MenuBO> selectAll(TenantId tenantId);
    List<MenuBO> selectAllByType(TenantId tenantId, String type);
    List<MenuBO> selectAllExcludingType(TenantId tenantId, String type);
    MenuBO selectById(TenantId tenantId, Long id);
    MenuBO insert(MenuBO menuBO);
    boolean update(MenuBO menuBO);
    boolean deleteById(TenantId tenantId, Long id);
    Pair<Long, List<MenuBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize, String name, String code, String type, Integer status);
    List<MenuBO> selectMenusByUserId(TenantId tenantId, Long userId, String roleCode,
                                     boolean parentSuperAdminSwitch, String excludeType);
    boolean existsByName(TenantId tenantId, String name, Long excludeId);
    boolean existsByPath(TenantId tenantId, String path, Long excludeId);
    List<MenuBO> selectByParentId(TenantId tenantId, Long parentId);
    List<MenuBO> selectByParentIdAndType(TenantId tenantId, Long parentId, String type);
}
