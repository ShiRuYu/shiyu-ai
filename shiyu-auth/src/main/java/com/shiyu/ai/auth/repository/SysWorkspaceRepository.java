package com.shiyu.ai.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.auth.domain.SysWorkspaceDO;
import com.shiyu.ai.auth.domain.bo.SysWorkspaceBO;
import com.shiyu.ai.auth.mapper.SysWorkspaceMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 工作空间数据仓储层
 *
 * @author shiyu-ai
 */
@Component
@Transactional
public class SysWorkspaceRepository {

    @Resource
    private SysWorkspaceMapper sysWorkspaceMapper;

    public Pair<Long, List<SysWorkspaceBO>> getAll(Number pageNumber, Number pageSize) {
        QueryWrapper queryWrapper = new QueryWrapper();
        long count = sysWorkspaceMapper.selectCountByQuery(queryWrapper);
        if (pageNumber != null && pageSize != null && pageSize.longValue() > 0) {
            queryWrapper.limit((pageNumber.longValue() - 1) * pageSize.longValue(), pageSize.intValue());
        }
        List<SysWorkspaceDO> sysWorkspaces = sysWorkspaceMapper.selectListByQuery(queryWrapper);
        return Pair.of(count, MapstructUtils.convert(sysWorkspaces, SysWorkspaceBO.class));
    }

    public SysWorkspaceBO getById(Long workspaceId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(SysWorkspaceDO::getWorkspaceId, workspaceId);
        SysWorkspaceDO sysWorkspaceDO = sysWorkspaceMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(sysWorkspaceDO, SysWorkspaceBO.class);
    }

    public SysWorkspaceBO create(SysWorkspaceBO sysWorkspaceBO) {
        SysWorkspaceDO sysWorkspaceDO = MapstructUtils.convert(sysWorkspaceBO, SysWorkspaceDO.class);
        
        // 使用 insertSelective 忽略 null 值，让数据库 DEFAULT 生效
        sysWorkspaceMapper.insertSelective(sysWorkspaceDO);
        return MapstructUtils.convert(sysWorkspaceDO, SysWorkspaceBO.class);
    }

    public SysWorkspaceBO update(SysWorkspaceBO sysWorkspaceBO) {
        SysWorkspaceDO sysWorkspaceDO = MapstructUtils.convert(sysWorkspaceBO, SysWorkspaceDO.class);
        sysWorkspaceMapper.update(sysWorkspaceDO);
        return MapstructUtils.convert(sysWorkspaceDO, SysWorkspaceBO.class);
    }

    public void deleteById(Long workspaceId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(SysWorkspaceDO::getWorkspaceId, workspaceId);
        sysWorkspaceMapper.deleteByQuery(queryWrapper);
    }
}
