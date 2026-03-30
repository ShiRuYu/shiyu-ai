package com.shiyu.ai.agent.auth.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.auth.service.RoleService;
import com.shiyu.ai.agent.dal.dataobject.RoleDO;
import com.shiyu.ai.agent.dal.mapper.RoleMapper;
import com.shiyu.ai.agent.domain.bo.RoleBO;
import com.shiyu.ai.agent.domain.vo.RolePageResponse;
import com.shiyu.ai.agent.domain.vo.RoleVO;
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

    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @Override
    public RolePageResponse getRoleList(Integer pageNo, Integer pageSize, String name) {
        log.info("获取角色列表，pageNo: {}, pageSize: {}, name: {}", pageNo, pageSize, name);
        
        // 构建查询条件
        QueryWrapper queryWrapper = new QueryWrapper();
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        List<RoleDO> allRoles = roleMapper.selectListByQuery(queryWrapper);
        
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
        
        // 构建查询条件
        QueryWrapper queryWrapper = new QueryWrapper();
        if (enable != null) {
            queryWrapper.eq("enable", enable);
        }
        List<RoleDO> allRoles = roleMapper.selectListByQuery(queryWrapper);
        
        return MapstructUtils.convert(allRoles, RoleBO.class);
    }

    @Override
    public boolean updateRole(Long id, RoleBO roleBO) {
        log.info("修改角色，id: {}", id);
        
        RoleDO existingRole = roleMapper.selectOneById(id);
        if (existingRole == null) {
            return false;
        }
        
        RoleDO updatedRole = MapstructUtils.convert(roleBO, RoleDO.class);
        updatedRole.setId(id);
        
        return roleMapper.update(updatedRole) > 0;
    }

    @Override
    public boolean deleteRole(Long id) {
        log.info("删除角色，id: {}", id);
        return roleMapper.deleteById(id) > 0;
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
        RoleDO newRole = MapstructUtils.convert(roleBO, RoleDO.class);
        
        roleMapper.insert(newRole);
        
        return true;
    }
}
