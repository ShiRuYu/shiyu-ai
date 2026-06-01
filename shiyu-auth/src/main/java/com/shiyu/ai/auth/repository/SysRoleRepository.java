package com.shiyu.ai.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.auth.domain.SysRoleDO;
import com.shiyu.ai.auth.domain.bo.SysRoleBO;
import com.shiyu.ai.auth.mapper.SysRoleMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色数据仓储�?
 *
 * @author shiyu-ai
 */
@Component
@Transactional
public class SysRoleRepository {

    @Resource
    private SysRoleMapper sysRoleMapper;

    public Pair<Long, List<SysRoleBO>> getAll(Integer pageNumber, Integer pageSize) {
        QueryWrapper queryWrapper = new QueryWrapper();
        long count = sysRoleMapper.selectCountByQuery(queryWrapper);
        if (pageNumber != null && pageSize != null && pageSize > 0) {
            queryWrapper.limit((pageNumber.longValue() - 1) * pageSize.longValue(), pageSize);
        }
        List<SysRoleDO> sysRoles = sysRoleMapper.selectListByQuery(queryWrapper);
        return Pair.of(count, MapstructUtils.convert(sysRoles, SysRoleBO.class));
    }

    public SysRoleBO getById(Long roleId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(SysRoleDO::getRoleId, roleId);
        SysRoleDO sysRoleDO = sysRoleMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(sysRoleDO, SysRoleBO.class);
    }

    public SysRoleBO create(SysRoleBO sysRoleBO) {
        SysRoleDO sysRoleDO = MapstructUtils.convert(sysRoleBO, SysRoleDO.class);
        
        // 使用 insertSelective 忽略 null 值，让数据库 DEFAULT 生效
        sysRoleMapper.insertSelective(sysRoleDO);
        return MapstructUtils.convert(sysRoleDO, SysRoleBO.class);
    }

    public SysRoleBO update(SysRoleBO sysRoleBO) {
        SysRoleDO sysRoleDO = MapstructUtils.convert(sysRoleBO, SysRoleDO.class);
        sysRoleMapper.update(sysRoleDO);
        return MapstructUtils.convert(sysRoleDO, SysRoleBO.class);
    }

    public void deleteById(Long roleId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(SysRoleDO::getRoleId, roleId);
        sysRoleMapper.deleteByQuery(queryWrapper);
    }
}

