package com.shiyu.ai.agent.biz.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.auth.MenuDO;
import com.shiyu.ai.agent.dal.dataobject.auth.RoleDO;
import com.shiyu.ai.agent.dal.mapper.auth.RoleMapper;
import com.shiyu.ai.agent.dal.mapper.auth.RoleMenuMapper;
import com.shiyu.ai.agent.domain.bo.MenuBO;
import com.shiyu.ai.agent.domain.bo.RoleBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 角色数据仓储层
 */
@Component
public class RoleRepository {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    /**
     * 分页查询角色列表
     */
    public Pair<Long, List<RoleBO>> selectPage(Number pageNo, Number pageSize, String name) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if (name != null && !name.isEmpty()) {
            queryWrapper.eq(RoleDO::getRoleName, name);
        }
        
        long count = roleMapper.selectCountByQuery(queryWrapper);
        
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }
        
        List<RoleDO> roleDOs = roleMapper.selectListByQuery(queryWrapper);
        
        return Pair.of(count, MapstructUtils.convert(roleDOs, RoleBO.class));
    }

    /**
     * 查询所有角色列表
     */
    public List<RoleBO> selectAll(String status) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(RoleDO::getStatus, status);
        }
        
        List<RoleDO> roleDOs = roleMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(roleDOs, RoleBO.class);
    }

    /**
     * 根据ID查询角色
     */
    public RoleBO selectById(Long id) {
        RoleDO roleDO = roleMapper.selectOneById(id);
        return MapstructUtils.convert(roleDO, RoleBO.class);
    }

    /**
     * 创建角色
     */
    public RoleBO insert(RoleBO roleBO) {
        RoleDO roleDO = MapstructUtils.convert(roleBO, RoleDO.class);
        roleMapper.insert(roleDO);
        roleBO.setId(roleDO.getId());
        return roleBO;
    }

    /**
     * 更新角色
     */
    public boolean update(RoleBO roleBO) {
        RoleDO roleDO = MapstructUtils.convert(roleBO, RoleDO.class);
        return roleMapper.update(roleDO) > 0;
    }

    /**
     * 删除角色
     */
    public boolean deleteById(Long id) {
        return roleMapper.deleteById(id) > 0;
    }

    /**
     * 根据角色ID查询菜单列表
     */
    public List<MenuBO> selectMenusByRoleId(Long roleId) {
        List<MenuDO> menuDOs = roleMenuMapper.selectMenusByRoleId(roleId);
        return MapstructUtils.convert(menuDOs, MenuBO.class);
    }

}
