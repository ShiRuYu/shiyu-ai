package com.shiyu.ai.agent.biz.auth.service.impl;

import com.shiyu.ai.dal.repository.RoleRepository;
import com.shiyu.ai.agent.biz.auth.service.RoleService;
import com.shiyu.ai.dal.dataobject.auth.UserWorkspaceRoleDO;
import com.shiyu.ai.dal.repository.UserWorkspaceRoleRepository;
import com.shiyu.ai.model.bo.RoleBO;
import com.shiyu.ai.model.vo.RolePageResponse;
import com.shiyu.ai.model.vo.RoleVO;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色服务实现类
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserWorkspaceRoleRepository userWorkspaceRoleRepository;

    public RoleServiceImpl(RoleRepository roleRepository, UserWorkspaceRoleRepository userWorkspaceRoleRepository) {
        this.roleRepository = roleRepository;
        this.userWorkspaceRoleRepository = userWorkspaceRoleRepository;
    }

    @Override
    public RolePageResponse getRoleList(Number pageNo, Number pageSize, String name) {
        log.info("获取角色列表，pageNo: {}, pageSize: {}, name: {}", pageNo, pageSize, name);
        
        Pair<Long, List<RoleBO>> result = roleRepository.selectPage(pageNo, pageSize, name);
        List<RoleBO> roleBOs = result.getRight();
        
        // 为每个角色填充权限菜单ID列表
        for (RoleBO roleBO : roleBOs) {
            List<Long> menuIds = roleRepository.selectMenuIdsByRoleId(roleBO.getId());
            roleBO.setPermissions(menuIds);
        }
        
        List<RoleVO> roleVOs = MapstructUtils.convert(roleBOs, RoleVO.class);
        
        RolePageResponse response = new RolePageResponse();
        response.setItems(roleVOs);
        response.setTotal(result.getLeft());
        
        return response;
    }

    @Override
    public List<RoleBO> getAllRoles(String status) {
        log.info("获取所有角色，status: {}", status);
        return roleRepository.selectAll(status);
    }

    @Override
    public boolean updateRole(Long id, RoleBO roleBO) {
        log.info("修改角色，id: {}", id);
        
        RoleBO existingRole = roleRepository.selectById(id);
        if (existingRole == null) {
            return false;
        }
        
        // 保存角色基本信息
        roleBO.setId(id);
        boolean success = roleRepository.update(roleBO);
        
        // 如果提供了permissions，则更新角色-菜单关联
        if (success && roleBO.getPermissions() != null) {
            // 先删除旧的关联
            roleRepository.deleteRoleMenus(id);
            // 再插入新的关联
            roleRepository.insertRoleMenus(id, roleBO.getPermissions());
        }
        
        return success;
    }

    @Override
    public boolean deleteRole(Long id) {
        log.info("删除角色，id: {}", id);
        
        // 先删除角色-菜单关联
        roleRepository.deleteRoleMenus(id);
        
        // 再删除角色
        return roleRepository.deleteById(id);
    }

    @Override
    public boolean removeUserRoles(Long roleId, List<Long> userIds) {
        Long workspaceId = LoginContextHolder.getCurrentWorkspaceId();
        log.info("取消分配角色，roleId: {}, userIds: {}, workspaceId: {}", roleId, userIds, workspaceId);
        if (userIds == null || userIds.isEmpty()) {
            return true;
        }
        Long tenantId = LoginContextHolder.getTenantId();
        for (Long userId : userIds) {
            com.mybatisflex.core.query.QueryWrapper qw = new com.mybatisflex.core.query.QueryWrapper();
            qw.eq(UserWorkspaceRoleDO::getUserId, userId)
               .eq(UserWorkspaceRoleDO::getRoleId, roleId)
               .eq(UserWorkspaceRoleDO::getWorkspaceId, workspaceId);
            if (tenantId != null) {
                qw.eq(UserWorkspaceRoleDO::getTenantId, tenantId);
            }
            userWorkspaceRoleRepository.deleteByQuery(qw);
        }
        return true;
    }

    @Override
    public boolean assignUserRoles(Long roleId, List<Long> userIds) {
        Long workspaceId = LoginContextHolder.getCurrentWorkspaceId();
        log.info("分配角色，roleId: {}, userIds: {}, workspaceId: {}", roleId, userIds, workspaceId);
        if (userIds == null || userIds.isEmpty()) {
            return true;
        }
        Long tenantId = LoginContextHolder.getTenantId();
        for (Long userId : userIds) {
            UserWorkspaceRoleDO uwr = new UserWorkspaceRoleDO();
            uwr.setUserId(userId);
            uwr.setWorkspaceId(workspaceId);
            uwr.setRoleId(roleId);
            uwr.setTenantId(tenantId);
            userWorkspaceRoleRepository.insert(uwr);
        }
        return true;
    }

    @Override
    public boolean createRole(RoleBO roleBO) {
        log.info("新增角色");
        
        // 保存角色基本信息
        RoleBO savedRole = roleRepository.insert(roleBO);
        
        // 如果提供了permissions，则保存角色-菜单关联
        if (roleBO.getPermissions() != null && !roleBO.getPermissions().isEmpty()) {
            roleRepository.insertRoleMenus(savedRole.getId(), roleBO.getPermissions());
        }
        
        return true;
    }
}
