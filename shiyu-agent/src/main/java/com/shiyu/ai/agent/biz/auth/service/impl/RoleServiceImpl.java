package com.shiyu.ai.agent.biz.auth.service.impl;

import com.shiyu.ai.agent.biz.auth.repository.RoleRepository;
import com.shiyu.ai.agent.biz.auth.service.RoleService;
import com.shiyu.ai.agent.domain.bo.RoleBO;
import com.shiyu.ai.agent.domain.vo.RolePageResponse;
import com.shiyu.ai.agent.domain.vo.RoleVO;
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

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public RolePageResponse getRoleList(Integer pageNo, Integer pageSize, String name) {
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
        
        roleBO.setId(id);
        return roleRepository.update(roleBO);
    }

    @Override
    public boolean deleteRole(Long id) {
        log.info("删除角色，id: {}", id);
        return roleRepository.deleteById(id);
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
        roleRepository.insert(roleBO);
        return true;
    }
}
