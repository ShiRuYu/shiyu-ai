package com.shiyu.ai.auth.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.auth.persistence.dataobject.MenuDO;
import com.shiyu.ai.auth.persistence.dataobject.RoleDO;
import com.shiyu.ai.auth.persistence.dataobject.RoleScopeMenuDO;
import com.shiyu.ai.auth.persistence.dataobject.RoleScopeAuthCodeDO;
import com.shiyu.ai.auth.persistence.dataobject.UserScopeRoleDO;
import com.shiyu.ai.auth.persistence.dataobject.TenantMenuDO;
import com.shiyu.ai.auth.persistence.mapper.MenuMapper;
import com.shiyu.ai.auth.persistence.mapper.RoleMapper;
import com.shiyu.ai.auth.persistence.mapper.RoleScopeMenuMapper;
import com.shiyu.ai.auth.persistence.mapper.RoleScopeAuthCodeMapper;
import com.shiyu.ai.auth.persistence.mapper.UserScopeRoleMapper;
import com.shiyu.ai.auth.domain.model.MenuBO;
import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.mybatisflex.core.tenant.TenantManager;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mybatisflex.core.query.QueryMethods.column;

/**
 * 角色数据仓储层
 */
@Component
public class RoleRepositoryImpl implements com.shiyu.ai.auth.port.repository.RoleRepository {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleScopeMenuMapper roleScopeMenuMapper;

    @Resource
    private MenuMapper menuMapper;

    @Resource
    private UserScopeRoleMapper userScopeRoleMapper;

    @Resource
    private RoleScopeAuthCodeMapper roleScopeAuthCodeMapper;

    /**
     * 分页查询角色列表
     */
    public Pair<Long, List<RoleBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize, String name) {
        requireTenant(tenantId);
        QueryWrapper countWrapper = new QueryWrapper();
        countWrapper.eq(RoleDO::getTenantId, tenantId.value())
                .and(RoleDO::getDelFlag).eq(0);
        if (name != null && !name.isEmpty()) {
            countWrapper.like(RoleDO::getName, name);
        }
        long count = roleMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(RoleDO::getTenantId, tenantId.value())
                .and(RoleDO::getDelFlag).eq(0);
        if (name != null && !name.isEmpty()) {
            queryWrapper.like(RoleDO::getName, name);
        }
        long page = pageNo == null ? 1 : pageNo.longValue();
        long size = pageSize == null ? 10 : pageSize.longValue();
        queryWrapper.limit((page - 1) * size, size);
        
        List<RoleDO> roleDOs = roleMapper.selectListByQuery(queryWrapper);
        
        return Pair.of(count, MapstructUtils.convert(roleDOs, RoleBO.class));
    }

    /**
     * 查询所有角色列表
     */
    public List<RoleBO> selectAll(TenantId tenantId, String status) {
        requireTenant(tenantId);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(RoleDO::getTenantId, tenantId.value())
                .and(RoleDO::getDelFlag).eq(0);
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(RoleDO::getStatus, status);
        }
        
        List<RoleDO> roleDOs = roleMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(roleDOs, RoleBO.class);
    }

    /**
     * 查询指定租户的角色。调用方必须先校验目标租户属于当前操作租户的
     * 自身或后代，本方法只负责按明确的 tenant_id 查询角色。
     */
    public List<RoleBO> selectAllByTenant(String status, TenantId tenantId) {
        long tenantValue = requireTenant(tenantId);
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(RoleDO::getTenantId).eq(tenantValue);
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(RoleDO::getStatus, status);
        }
        List<RoleDO> roleDOs = TenantManager.withoutTenantCondition(
                () -> roleMapper.selectListByQuery(queryWrapper));
        return MapstructUtils.convert(roleDOs, RoleBO.class);
    }

    /**
     * 根据ID查询角色
     */
    public RoleBO selectById(Long id, TenantId tenantId) {
        long tenantValue = requireTenant(tenantId);
        if (id == null) {
            return null;
        }
        QueryWrapper query = QueryWrapper.create()
                .where(RoleDO::getId).eq(id)
                .and(RoleDO::getTenantId).eq(tenantValue);
        RoleDO roleDO = TenantManager.withoutTenantCondition(
                () -> roleMapper.selectOneByQuery(query));
        return MapstructUtils.convert(roleDO, RoleBO.class);
    }

    /**
     * 创建角色
     */
    public RoleBO insert(RoleBO roleBO) {
        if (roleBO == null || roleBO.getTenantId() == null || roleBO.getTenantId() <= 0) {
            throw new IllegalArgumentException("tenantId must be provided for role creation");
        }
        RoleDO roleDO = MapstructUtils.convert(roleBO, RoleDO.class);
        
        // 如果 code 为空，使用 name 作为 code
        if (roleDO.getCode() == null || roleDO.getCode().isEmpty()) {
            roleDO.setCode(roleDO.getName());
        }
        
        // 使用 insertSelective 忽略 null 值，让数据库 DEFAULT 生效
        roleMapper.insertSelective(roleDO);
        roleBO.setId(roleDO.getId());
        return roleBO;
    }

    /**
     * 更新角色
     */
    public boolean update(RoleBO roleBO) {
        if (roleBO == null || roleBO.getId() == null
                || roleBO.getTenantId() == null || roleBO.getTenantId() <= 0) {
            return false;
        }
        RoleDO roleDO = MapstructUtils.convert(roleBO, RoleDO.class);
        return TenantManager.withoutTenantCondition(() -> roleMapper.updateByQuery(
                roleDO,
                QueryWrapper.create().where(RoleDO::getId).eq(roleBO.getId())
                        .and(RoleDO::getTenantId).eq(roleBO.getTenantId())
                        .and(RoleDO::getDelFlag).eq(0))) > 0;
    }

    /**
     * 删除角色以及该角色在所有租户作用域下的授权关系。
     */
    public boolean deleteRoleAndRelations(Long roleId, TenantId tenantId) {
        long tenantValue = requireTenant(tenantId);
        if (roleId == null) {
            return false;
        }
        userScopeRoleMapper.deleteByQuery(QueryWrapper.create()
                .where(UserScopeRoleDO::getRoleId).eq(roleId)
                .and(UserScopeRoleDO::getTenantId).eq(tenantValue));
        roleScopeMenuMapper.deleteByQuery(QueryWrapper.create()
                .where(RoleScopeMenuDO::getRoleId).eq(roleId)
                .and(RoleScopeMenuDO::getTenantId).eq(tenantValue));
        roleScopeAuthCodeMapper.deleteByQuery(QueryWrapper.create()
                .where(RoleScopeAuthCodeDO::getRoleId).eq(roleId)
                .and(RoleScopeAuthCodeDO::getTenantId).eq(tenantValue));
        return TenantManager.withoutTenantCondition(() -> roleMapper.deleteByQuery(
                QueryWrapper.create().where(RoleDO::getId).eq(roleId)
                        .and(RoleDO::getTenantId).eq(tenantValue))) > 0;
    }

    /**
     * 校验角色是否属于当前租户作用域且处于有效状态。
     */
    public boolean isRoleInScope(Long roleId, TenantId currentTenantId) {
        long tenantValue = requireTenant(currentTenantId);
        if (roleId == null) {
            return false;
        }
        QueryWrapper qw = QueryWrapper.create()
                .where(RoleDO::getId).eq(roleId)
                .and(RoleDO::getStatus).eq(1)
                .and(RoleDO::getDelFlag).eq(0)
                .and(RoleDO::getTenantId).eq(tenantValue);
        return roleMapper.selectCountByQuery(qw) > 0;
    }

    /**
     * 校验角色是否属于指定目标租户。
     */
    public boolean isRoleOwnedByTenant(Long roleId, TenantId tenantId) {
        long tenantValue = requireTenant(tenantId);
        if (roleId == null) {
            return false;
        }
        QueryWrapper qw = QueryWrapper.create()
                .where(RoleDO::getId).eq(roleId)
                .and(RoleDO::getTenantId).eq(tenantValue)
                .and(RoleDO::getStatus).eq(1)
                .and(RoleDO::getDelFlag).eq(0);
        return TenantManager.withoutTenantCondition(
                () -> roleMapper.selectCountByQuery(qw) > 0);
    }

    /**
     * 校验菜单均存在、有效且属于允许的租户范围。
     */
    public boolean areMenusInTenantScope(List<Long> menuIds, List<Long> allowedTenantIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return true;
        }
        if (allowedTenantIds == null || allowedTenantIds.isEmpty()) {
            return false;
        }
        List<Long> distinctMenuIds = menuIds.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (distinctMenuIds.size() != menuIds.size()) {
            return false;
        }
        QueryWrapper query = QueryWrapper.create()
                .from(MenuDO.class)
                .innerJoin(TenantMenuDO.class)
                    .on(column(MenuDO::getId).eq(column(TenantMenuDO::getMenuId)))
                .where(MenuDO::getId).in(distinctMenuIds)
                .and(TenantMenuDO::getTenantId).in(allowedTenantIds)
                .and(MenuDO::getTenantId).in(allowedTenantIds)
                .and(MenuDO::getStatus).eq(1)
                .and(MenuDO::getDelFlag).eq(0);
        long count = TenantManager.withoutTenantCondition(
                () -> menuMapper.selectCountByQuery(query));
        return count == distinctMenuIds.size();
    }

    /**
     * 批量查询多个角色的菜单ID列表（修复 N+1）
     */
    public Map<Long, List<Long>> selectMenuIdsByRoleIds(TenantId tenantId, List<Long> roleIds) {
        requireTenant(tenantId);
        if (roleIds == null || roleIds.isEmpty()) {
            return Map.of();
        }
        QueryWrapper qw = QueryWrapper.create()
            .select(column(RoleScopeMenuDO::getRoleId), column(RoleScopeMenuDO::getMenuId))
            .from(RoleScopeMenuDO.class)
            .innerJoin(MenuDO.class)
                .on(column(RoleScopeMenuDO::getMenuId).eq(column(MenuDO::getId)))
            .where(RoleScopeMenuDO::getRoleId).in(roleIds)
            .and(MenuDO::getStatus).eq(1)
            .and(MenuDO::getDelFlag).eq(0);
        qw.and(RoleScopeMenuDO::getTenantId).eq(tenantId.value());
        qw.orderBy(column(MenuDO::getOrder).asc(), column(MenuDO::getId).asc());
        List<RoleScopeMenuDO> list = TenantManager.withoutTenantCondition(
                () -> roleScopeMenuMapper.selectListByQuery(qw));
        return list.stream().collect(Collectors.groupingBy(
            RoleScopeMenuDO::getRoleId,
            Collectors.mapping(RoleScopeMenuDO::getMenuId, Collectors.toList())
        ));
    }

    public List<Long> selectMenuIdsByRoleId(Long roleId, TenantId roleTenantId, TenantId tenantId) {
        long tenantValue = requireTenant(tenantId);
        requireTenant(roleTenantId);
        if (!roleTenantId.equals(tenantId)) {
            return List.of();
        }
        QueryWrapper qw = QueryWrapper.create()
            .select(column(RoleScopeMenuDO::getMenuId))
            .from(RoleScopeMenuDO.class)
            .innerJoin(MenuDO.class)
                .on(column(RoleScopeMenuDO::getMenuId).eq(column(MenuDO::getId)))
            .where(RoleScopeMenuDO::getRoleId).eq(roleId)
            .and(RoleScopeMenuDO::getTenantId).eq(tenantValue)
            .and(MenuDO::getStatus).eq(1)
            .and(MenuDO::getDelFlag).eq(0)
            .orderBy(column(MenuDO::getOrder).asc(), column(MenuDO::getId).asc());
        return TenantManager.withoutTenantCondition(
                () -> roleScopeMenuMapper.selectListByQuery(qw)).stream()
                .map(RoleScopeMenuDO::getMenuId)
                .toList();
    }

    public void insertRoleMenus(Long roleId, TenantId roleTenantId, TenantId tenantId, List<Long> menuIds) {
        long tenantValue = requireTenant(tenantId);
        requireTenant(roleTenantId);
        if (!roleTenantId.equals(tenantId)) {
            throw new IllegalArgumentException("role tenant and scope tenant must match");
        }
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        List<RoleScopeMenuDO> list = menuIds.stream().map(menuId -> {
            RoleScopeMenuDO rwm = new RoleScopeMenuDO();
            rwm.setRoleId(roleId);
            rwm.setMenuId(menuId);
            rwm.setTenantId(tenantValue);
            return rwm;
        }).toList();
        roleScopeMenuMapper.insertBatch(list);
    }

    public void deleteRoleMenus(Long roleId, TenantId roleTenantId, TenantId tenantId) {
        long tenantValue = requireTenant(tenantId);
        requireTenant(roleTenantId);
        if (!roleTenantId.equals(tenantId)) {
            return;
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(RoleScopeMenuDO::getRoleId, roleId);
        queryWrapper.eq(RoleScopeMenuDO::getTenantId, tenantValue);
        roleScopeMenuMapper.deleteByQuery(queryWrapper);
    }

    private long requireTenant(TenantId tenantId) {
        if (tenantId == null || tenantId.value() <= 0) {
            throw new IllegalArgumentException("tenantId is required for role repository query");
        }
        return tenantId.value();
    }

}

