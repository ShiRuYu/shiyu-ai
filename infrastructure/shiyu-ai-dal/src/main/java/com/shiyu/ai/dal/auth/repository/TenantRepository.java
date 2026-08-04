package com.shiyu.ai.dal.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.auth.dataobject.*;
import com.shiyu.ai.dal.auth.mapper.*;
import com.shiyu.ai.auth.domain.model.TenantBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.common.core.utils.PasswordUtils;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.mybatisflex.core.tenant.TenantManager;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TenantRepository implements com.shiyu.ai.auth.port.repository.TenantRepository {

    @Resource
    private TenantMapper tenantMapper;

    @Resource
    private TenantQuotaMapper tenantQuotaMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private MenuMapper menuMapper;

    @Resource
    private AuthCodeMapper authCodeMapper;

    @Resource
    private UserScopeRoleMapper userScopeRoleMapper;

    @Resource
    private RoleScopeMenuMapper roleScopeMenuMapper;

    @Resource
    private RoleScopeAuthCodeMapper roleScopeAuthCodeMapper;

    @Resource
    private TenantMenuMapper tenantMenuMapper;

    @Resource
    private TenantAuthCodeMapper tenantAuthCodeMapper;

    public Pair<Long, List<TenantBO>> selectPage(Number pageNo, Number pageSize,
                                                 String name, String code, Integer status) {
        QueryWrapper countWrapper = QueryWrapper.create()
                .where(TenantDO::getDelFlag).eq(0);
        Long currentTenantId = UserContextHolder.getCurrentTenantId();
        if (currentTenantId != null) {
            countWrapper.in(TenantDO::getId, selectDescendantIds(currentTenantId));
        }
        if (name != null && !name.isBlank()) {
            countWrapper.like(TenantDO::getName, name);
        }
        if (code != null && !code.isBlank()) countWrapper.like(TenantDO::getCode, code);
        if (status != null) countWrapper.eq(TenantDO::getStatus, status);
        long count = tenantMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(TenantDO::getDelFlag).eq(0);
        if (currentTenantId != null) {
            queryWrapper.in(TenantDO::getId, selectDescendantIds(currentTenantId));
        }
        if (name != null && !name.isBlank()) {
            queryWrapper.like(TenantDO::getName, name);
        }
        if (code != null && !code.isBlank()) queryWrapper.like(TenantDO::getCode, code);
        if (status != null) queryWrapper.eq(TenantDO::getStatus, status);
        long page = pageNo == null ? 1 : pageNo.longValue();
        long size = pageSize == null ? 10 : pageSize.longValue();
        queryWrapper.limit((page - 1) * size, size);
        queryWrapper.orderBy(TenantDO::getId, true);

        List<TenantDO> tenantDOs = tenantMapper.selectListByQuery(queryWrapper);
        return Pair.of(count, MapstructUtils.convert(tenantDOs, TenantBO.class));
    }

    public List<TenantBO> selectAll() {
        List<TenantDO> tenantDOs = TenantManager.withoutTenantCondition(tenantMapper::selectAll);
        return MapstructUtils.convert(tenantDOs, TenantBO.class);
    }

    public TenantBO selectById(Long id) {
        TenantDO tenantDO = tenantMapper.selectOneById(id);
        return MapstructUtils.convert(tenantDO, TenantBO.class);
    }

    /**
     * 获取指定租户所在租户树的根租户。
     * 当前数据模型以 parent_id 为 null 的租户作为根租户。
     */
    public Long selectRootTenantId(Long tenantId) {
        if (tenantId == null) {
            return null;
        }
        Map<Long, TenantDO> tenantMap = tenantMapper.selectAll().stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(TenantDO::getId, item -> item, (first, ignored) -> first));
        TenantDO current = tenantMap.get(tenantId);
        Set<Long> visited = new HashSet<>();
        while (current != null && current.getParentId() != null && visited.add(current.getId())) {
            current = tenantMap.get(current.getParentId());
        }
        return current == null ? null : current.getId();
    }

    public TenantBO insert(TenantBO tenantBO) {
        TenantDO tenantDO = MapstructUtils.convert(tenantBO, TenantDO.class);
        tenantMapper.insertSelective(tenantDO);
        tenantBO.setId(tenantDO.getId());
        initializeTenantSecurity(tenantBO);
        return tenantBO;
    }

    private void initializeTenantSecurity(TenantBO tenantBO) {
        Long tenantId = tenantBO.getId();
        if (tenantId == null) {
            return;
        }
        RoleDO superRole = new RoleDO();
        superRole.setCode("tenant_super");
        superRole.setName(tenantBO.getAdminRoleName() == null
                || tenantBO.getAdminRoleName().isBlank()
                ? "租户超级管理员" : tenantBO.getAdminRoleName());
        superRole.setTenantId(tenantId);
        superRole.setStatus(1);
        superRole.setDelFlag(0);
        roleMapper.insertSelective(superRole);

        List<Long> requestedMenuIds = resolveSourceMenuIds(tenantBO.getMenuIds());
        List<Long> authCodeIds = resolveSourceAuthCodeIds(tenantBO.getAuthCodeIds());

        // 菜单属于租户私有数据。创建子租户时不能把父租户 menu_id
        // 直接写入 role_scope_menu，而要复制菜单树并使用新租户自己的 menu_id。
        List<Long> menuIds = cloneMenusForTenant(tenantId, requestedMenuIds);
        if (!menuIds.isEmpty()) {
            tenantMenuMapper.insertBatch(menuIds.stream().map(menuId -> {
                TenantMenuDO item = new TenantMenuDO();
                item.setTenantId(tenantId);
                item.setMenuId(menuId);
                item.setStatus(1);
                return item;
            }).toList());
            roleScopeMenuMapper.insertBatch(menuIds.stream().map(menuId -> {
                RoleScopeMenuDO item = new RoleScopeMenuDO();
                item.setRoleId(superRole.getId());
                item.setTenantId(tenantId);
                item.setMenuId(menuId);
                item.setStatus(1);
                item.setDelFlag(0);
                return item;
            }).toList());
        }
        if (!authCodeIds.isEmpty()) {
            tenantAuthCodeMapper.insertBatch(authCodeIds.stream().map(authCodeId -> {
                TenantAuthCodeDO item = new TenantAuthCodeDO();
                item.setTenantId(tenantId);
                item.setAuthCodeId(authCodeId);
                item.setStatus(1);
                return item;
            }).toList());
            roleScopeAuthCodeMapper.insertBatch(authCodeIds.stream().map(authCodeId -> {
                RoleScopeAuthCodeDO item = new RoleScopeAuthCodeDO();
                item.setRoleId(superRole.getId());
                item.setTenantId(tenantId);
                item.setAuthCodeId(authCodeId);
                item.setStatus(1);
                item.setDelFlag(0);
                return item;
            }).toList());
        }

        UserDO admin = new UserDO();
        admin.setUsername(tenantBO.getAdminUsername() == null
                || tenantBO.getAdminUsername().isBlank()
                ? tenantBO.getCode() + "_admin" : tenantBO.getAdminUsername());
        admin.setPassword(PasswordUtils.encode(
                tenantBO.getAdminPassword() == null || tenantBO.getAdminPassword().isBlank()
                        ? PasswordUtils.generateDefaultPassword() : tenantBO.getAdminPassword()));
        admin.setStatus(1);
        admin.setDelFlag(0);
        userMapper.insertSelective(admin);

        UserScopeRoleDO assignment = new UserScopeRoleDO();
        assignment.setUserId(admin.getId());
        assignment.setTenantId(tenantId);
        assignment.setRoleId(superRole.getId());
        assignment.setStatus(1);
        assignment.setDelFlag(0);
        userScopeRoleMapper.insert(assignment);
    }

    private List<Long> resolveSourceMenuIds(List<Long> requestedMenuIds) {
        Long sourceTenantId = UserContextHolder.getCurrentTenantId();
        if (sourceTenantId == null) {
            return List.of();
        }
        List<Long> availableIds = TenantManager.withoutTenantCondition(
                () -> menuMapper.selectListByQuery(QueryWrapper.create()
                        .where(MenuDO::getTenantId).eq(sourceTenantId)
                        .and(MenuDO::getStatus).eq(1)
                        .and(MenuDO::getDelFlag).eq(0)))
                .stream().map(MenuDO::getId).filter(Objects::nonNull).toList();
        if (requestedMenuIds == null || requestedMenuIds.isEmpty()) {
            return availableIds;
        }
        Set<Long> available = new HashSet<>(availableIds);
        return requestedMenuIds.stream()
                .filter(Objects::nonNull)
                .filter(available::contains)
                .distinct()
                .toList();
    }

    private List<Long> resolveSourceAuthCodeIds(List<Long> requestedAuthCodeIds) {
        Long sourceTenantId = UserContextHolder.getCurrentTenantId();
        if (sourceTenantId == null) {
            return List.of();
        }
        List<Long> availableIds = tenantAuthCodeMapper.selectListByQuery(QueryWrapper.create()
                        .where(TenantAuthCodeDO::getTenantId).eq(sourceTenantId)
                        .and(TenantAuthCodeDO::getStatus).eq(1))
                .stream().map(TenantAuthCodeDO::getAuthCodeId)
                .filter(Objects::nonNull)
                .filter(id -> {
                    AuthCodeDO authCode = authCodeMapper.selectOneById(id);
                    return authCode != null
                            && authCode.getStatus() != null && authCode.getStatus() == 1
                            && (authCode.getDelFlag() == null || authCode.getDelFlag() == 0);
                })
                .distinct()
                .toList();
        if (requestedAuthCodeIds == null || requestedAuthCodeIds.isEmpty()) {
            return availableIds;
        }
        Set<Long> available = new HashSet<>(availableIds);
        return requestedAuthCodeIds.stream()
                .filter(Objects::nonNull)
                .filter(available::contains)
                .distinct()
                .toList();
    }

    /**
     * 复制请求中选择的菜单及其祖先节点，返回新租户菜单 ID。
     *
     * <p>租户创建请求中的 menuIds 来自当前操作租户，因此这里以当前租户
     * 作为源租户。祖先节点必须一并复制，否则前端菜单树会出现断链。</p>
     */
    private List<Long> cloneMenusForTenant(Long targetTenantId, List<Long> requestedMenuIds) {
        if (targetTenantId == null || requestedMenuIds == null || requestedMenuIds.isEmpty()) {
            return List.of();
        }

        Long sourceTenantId = UserContextHolder.getCurrentTenantId();
        if (sourceTenantId == null) {
            return List.of();
        }

        QueryWrapper sourceQuery = QueryWrapper.create()
                .where(MenuDO::getTenantId).eq(sourceTenantId)
                .and(MenuDO::getDelFlag).eq(0);
        List<MenuDO> sourceMenus = TenantManager.withoutTenantCondition(
                () -> menuMapper.selectListByQuery(sourceQuery));
        if (sourceMenus.isEmpty()) {
            return List.of();
        }

        Map<Long, MenuDO> menuById = sourceMenus.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(MenuDO::getId, item -> item, (first, ignored) -> first));
        Set<Long> selectedIds = new LinkedHashSet<>();
        for (Long requestedId : requestedMenuIds) {
            MenuDO menu = menuById.get(requestedId);
            while (menu != null && menu.getId() != null && selectedIds.add(menu.getId())) {
                menu = menu.getParentId() == null ? null : menuById.get(menu.getParentId());
            }
        }

        List<MenuDO> menusToClone = sourceMenus.stream()
                .filter(item -> item.getId() != null && selectedIds.contains(item.getId()))
                .sorted(Comparator.comparingInt(item -> menuDepth(item, menuById)))
                .toList();
        Map<Long, Long> idMapping = new HashMap<>();
        List<Long> clonedIds = new ArrayList<>(menusToClone.size());

        for (MenuDO source : menusToClone) {
            MenuDO target = new MenuDO();
            target.setName(source.getName());
            target.setCode(source.getCode());
            target.setType(source.getType());
            target.setParentId(source.getParentId() == null
                    ? null : idMapping.get(source.getParentId()));
            target.setTenantId(targetTenantId);
            target.setPath(source.getPath());
            target.setRedirect(source.getRedirect());
            target.setIcon(source.getIcon());
            target.setComponent(source.getComponent());
            target.setLayout(source.getLayout());
            target.setKeepAlive(source.getKeepAlive());
            target.setMethod(source.getMethod());
            target.setDescription(source.getDescription());
            target.setShow(source.getShow());
            target.setStatus(source.getStatus());
            target.setOrder(source.getOrder());
            target.setDelFlag(0);
            target.setCreateBy("system");
            target.setUpdateBy("system");
            menuMapper.insertSelective(target);
            idMapping.put(source.getId(), target.getId());
            clonedIds.add(target.getId());
        }
        return clonedIds;
    }

    private int menuDepth(MenuDO menu, Map<Long, MenuDO> menuById) {
        int depth = 0;
        Set<Long> visited = new HashSet<>();
        MenuDO current = menu;
        while (current != null && current.getParentId() != null
                && visited.add(current.getId())) {
            depth++;
            current = menuById.get(current.getParentId());
        }
        return depth;
    }

    public boolean update(TenantBO tenantBO) {
        TenantDO tenantDO = MapstructUtils.convert(tenantBO, TenantDO.class);
        return tenantMapper.update(tenantDO) > 0;
    }

    public boolean deleteById(Long id) {
        return tenantMapper.deleteById(id) > 0;
    }

    public boolean existsByCode(String code, Long excludeId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(TenantDO::getCode, code);
        if (excludeId != null) {
            qw.ne(TenantDO::getId, excludeId);
        }
        return tenantMapper.selectCountByQuery(qw) > 0;
    }

    /**
     * 级联删除租户及其所有关联数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void cascadeDelete(Long tenantId) {
        // 先找出所有后代子租户，全部级联删除
        Set<Long> allIds = new LinkedHashSet<>(selectDescendantIds(tenantId));
        allIds.add(tenantId);
        Set<Long> candidateUserIds = TenantManager.withoutTenantCondition(
                () -> userScopeRoleMapper.selectListByQuery(QueryWrapper.create()
                                .where(UserScopeRoleDO::getTenantId).in(allIds))
                        .stream().map(UserScopeRoleDO::getUserId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()));
        Set<Long> candidateAuthCodeIds = TenantManager.withoutTenantCondition(
                () -> tenantAuthCodeMapper.selectListByQuery(QueryWrapper.create()
                                .where(TenantAuthCodeDO::getTenantId).in(allIds))
                        .stream().map(TenantAuthCodeDO::getAuthCodeId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()));

        TenantManager.withoutTenantCondition(() -> {
            for (Long id : allIds) {
                tenantQuotaMapper.deleteByQuery(QueryWrapper.create()
                        .eq(TenantQuotaDO::getTenantId, id));
                roleScopeMenuMapper.deleteByQuery(QueryWrapper.create()
                        .eq(RoleScopeMenuDO::getTenantId, id));
                roleScopeAuthCodeMapper.deleteByQuery(QueryWrapper.create()
                        .eq(RoleScopeAuthCodeDO::getTenantId, id));
                tenantMenuMapper.deleteByQuery(QueryWrapper.create()
                        .eq(TenantMenuDO::getTenantId, id));
                tenantAuthCodeMapper.deleteByQuery(QueryWrapper.create()
                        .eq(TenantAuthCodeDO::getTenantId, id));
                userScopeRoleMapper.deleteByQuery(QueryWrapper.create()
                        .eq(UserScopeRoleDO::getTenantId, id));
                roleMapper.deleteByQuery(QueryWrapper.create().eq(RoleDO::getTenantId, id));
                menuMapper.deleteByQuery(QueryWrapper.create().eq(MenuDO::getTenantId, id));
            }
            tenantMapper.deleteByQuery(QueryWrapper.create()
                    .where(TenantDO::getId).in(allIds));
            return null;
        });
        for (Long userId : candidateUserIds) {
            long remainingAssignments = TenantManager.withoutTenantCondition(
                    () -> userScopeRoleMapper.selectCountByQuery(QueryWrapper.create()
                            .where(UserScopeRoleDO::getUserId).eq(userId)
                            .and(UserScopeRoleDO::getStatus).eq(1)
                            .and(UserScopeRoleDO::getDelFlag).eq(0)));
            if (remainingAssignments == 0) {
                userMapper.deleteById(userId);
            }
        }
        for (Long authCodeId : candidateAuthCodeIds) {
            long remainingTenantRelations = TenantManager.withoutTenantCondition(
                    () -> tenantAuthCodeMapper.selectCountByQuery(QueryWrapper.create()
                            .where(TenantAuthCodeDO::getAuthCodeId).eq(authCodeId)));
            if (remainingTenantRelations == 0) {
                authCodeMapper.deleteById(authCodeId);
            }
        }
    }

    /**
     * 查询指定租户的所有后代租户 ID（包含自身）
     */
    public List<Long> selectDescendantIds(Long rootId) {
        // 租户树查询用于校验父子关系，不能被当前业务租户的自动过滤截断。
        List<TenantDO> all = TenantManager.withoutTenantCondition(tenantMapper::selectAll);
        // parentId → childrenId 映射
        Map<Long, List<Long>> childrenMap = new HashMap<>();
        for (TenantDO t : all) {
            if (t.getParentId() != null) {
                childrenMap.computeIfAbsent(t.getParentId(), k -> new ArrayList<>()).add(t.getId());
            }
        }
        // BFS 收集所有后代
        Set<Long> result = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();
        if (rootId != null) queue.add(rootId);
        while (!queue.isEmpty()) {
            Long cur = queue.poll();
            if (!result.add(cur)) continue; // 已访问
            List<Long> children = childrenMap.get(cur);
            if (children != null) queue.addAll(children);
        }
        return new ArrayList<>(result);
    }
}
