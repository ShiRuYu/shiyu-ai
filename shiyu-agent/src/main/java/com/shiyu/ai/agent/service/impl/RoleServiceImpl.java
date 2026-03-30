package com.shiyu.ai.agent.service.impl;

import com.shiyu.ai.agent.domain.MenuDO;
import com.shiyu.ai.agent.domain.RoleDO;
import com.shiyu.ai.agent.domain.bo.MenuBO;
import com.shiyu.ai.agent.domain.bo.RoleBO;
import com.shiyu.ai.agent.domain.vo.RolePageResponse;
import com.shiyu.ai.agent.domain.vo.RoleVO;
import com.shiyu.ai.agent.service.RoleService;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色服务实现类（模拟数据）
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    // 模拟角色数据
    private final Map<Long, RoleDO> roleDatabase = new HashMap<>();

    public RoleServiceImpl() {
        initMockData();
    }

    private void initMockData() {
        RoleDO role1 = new RoleDO();
        role1.setId(1L);
        role1.setCode("SUPER_ADMIN");
        role1.setName("超级管理员");
        role1.setEnable(true);
        role1.setPermissionIds(new Long[]{});

        RoleDO role2 = new RoleDO();
        role2.setId(2L);
        role2.setCode("ROLE_QA");
        role2.setName("质检员");
        role2.setEnable(true);
        role2.setPermissionIds(new Long[]{1L, 2L, 3L, 4L, 5L, 9L, 10L, 11L, 12L, 14L, 15L});

        roleDatabase.put(1L, role1);
        roleDatabase.put(2L, role2);
    }

    @Override
    public RolePageResponse getRoleList(Integer pageNo, Integer pageSize, String name) {
        log.info("获取角色列表，pageNo: {}, pageSize: {}, name: {}", pageNo, pageSize, name);
        
        List<RoleDO> allRoles = new ArrayList<>(roleDatabase.values());
        
        // 过滤角色名称
        if (name != null && !name.isEmpty()) {
            String finalName = name;
            allRoles = allRoles.stream()
                    .filter(r -> r.getName().contains(finalName))
                    .collect(Collectors.toList());
        }
        
        // 分页
        int total = allRoles.size();
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        if (fromIndex >= total) {
            RolePageResponse response = new RolePageResponse();
            response.setPageData(new ArrayList<>());
            response.setTotal(0L);
            return response;
        }
        
        List<RoleDO> pageData = allRoles.subList(fromIndex, toIndex);
        List<RoleVO> roleVOs = MapstructUtils.convert(pageData, RoleVO.class);
        
        RolePageResponse response = new RolePageResponse();
        response.setPageData(roleVOs);
        response.setTotal((long) total);
        
        return response;
    }

    @Override
    public List<RoleBO> getAllRoles(Boolean enable) {
        log.info("获取所有角色，enable: {}", enable);
        
        List<RoleDO> allRoles = new ArrayList<>(roleDatabase.values());
        
        // 过滤启用状态
        if (enable != null) {
            Boolean finalEnable = enable;
            allRoles = allRoles.stream()
                    .filter(r -> finalEnable.equals(r.getEnable()))
                    .collect(Collectors.toList());
        }
        
        return MapstructUtils.convert(allRoles, RoleBO.class);
    }

    @Override
    public boolean updateRole(Long id, RoleBO roleBO) {
        log.info("修改角色，id: {}", id);
        
        RoleDO existingRole = roleDatabase.get(id);
        if (existingRole == null) {
            return false;
        }
        
        RoleDO updatedRole = MapstructUtils.convert(roleBO, RoleDO.class);
        updatedRole.setId(id);
        roleDatabase.put(id, updatedRole);
        
        return true;
    }

    @Override
    public boolean deleteRole(Long id) {
        log.info("删除角色，id: {}", id);
        RoleDO removed = roleDatabase.remove(id);
        return removed != null;
    }

    @Override
    public boolean removeUserRoles(Long id, List<Long> userIds) {
        log.info("取消分配角色，id: {}, userIds: {}", id, userIds);
        // 模拟操作
        return true;
    }

    @Override
    public boolean assignUserRoles(Long id, List<Long> userIds) {
        log.info("分配角色，id: {}, userIds: {}", id, userIds);
        // 模拟操作
        return true;
    }

    @Override
    public boolean createRole(RoleBO roleBO) {
        log.info("新增角色");
        Long newId = roleDatabase.keySet().stream().max(Long::compareTo).orElse(0L) + 1;
        
        RoleDO newRole = MapstructUtils.convert(roleBO, RoleDO.class);
        newRole.setId(newId);
        roleDatabase.put(newId, newRole);
        
        return true;
    }
}
