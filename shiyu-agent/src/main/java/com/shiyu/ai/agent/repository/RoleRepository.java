package com.shiyu.ai.agent.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.RoleDO;
import com.shiyu.ai.agent.dal.mapper.RoleMapper;
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

    /**
     * 分页查询角色列表
     */
    public Pair<Long, List<RoleBO>> selectPage(Integer pageNo, Integer pageSize, String name) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        
        long total = roleMapper.selectCountByQuery(queryWrapper);
        
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }
        
        List<RoleDO> roleDOs = roleMapper.selectListByQuery(queryWrapper);
        List<RoleBO> roleBOs = MapstructUtils.convert(roleDOs, RoleBO.class);
        
        return Pair.of(total, roleBOs);
    }

    /**
     * 查询所有角色
     */
    public List<RoleBO> selectAll(Boolean enable) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if (enable != null) {
            queryWrapper.eq("enable", enable);
        }
        List<RoleDO> roleDOs = roleMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(roleDOs, RoleBO.class);
    }

    /**
     * 根据ID查询角色
     */
    public RoleBO selectOneById(Long id) {
        RoleDO roleDO = roleMapper.selectOneById(id);
        return MapstructUtils.convert(roleDO, RoleBO.class);
    }

    /**
     * 插入角色
     */
    public RoleBO insert(RoleBO roleBO) {
        RoleDO roleDO = MapstructUtils.convert(roleBO, RoleDO.class);
        roleMapper.insert(roleDO);
        return MapstructUtils.convert(roleDO, RoleBO.class);
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
}
