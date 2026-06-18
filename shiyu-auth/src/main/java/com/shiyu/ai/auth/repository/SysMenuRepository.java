package com.shiyu.ai.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.auth.domain.SysMenuDO;
import com.shiyu.ai.auth.domain.bo.SysMenuBO;
import com.shiyu.ai.auth.mapper.SysMenuMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 菜单数据仓储
 *
 * @author shiyu-ai
 */
@Component
@Transactional
public class SysMenuRepository {

    @Resource
    private SysMenuMapper sysMenuMapper;

    public Pair<Long, List<SysMenuBO>> getAll(Number pageNumber, Number pageSize) {
        QueryWrapper queryWrapper = new QueryWrapper();
        long count = sysMenuMapper.selectCountByQuery(queryWrapper);
        if (pageNumber != null && pageSize != null && pageSize.longValue() > 0) {
            queryWrapper.limit((pageNumber.longValue() - 1) * pageSize.longValue(), pageSize.intValue());
        }
        List<SysMenuDO> sysMenus = sysMenuMapper.selectListByQuery(queryWrapper);
        return Pair.of(count, MapstructUtils.convert(sysMenus, SysMenuBO.class));
    }

    public SysMenuBO getById(Long menuId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(SysMenuDO::getMenuId, menuId);
        SysMenuDO sysMenuDO = sysMenuMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(sysMenuDO, SysMenuBO.class);
    }

    public SysMenuBO create(SysMenuBO sysMenuBO) {
        SysMenuDO sysMenuDO = MapstructUtils.convert(sysMenuBO, SysMenuDO.class);
        
        // 使用 insertSelective 忽略 null 值，让数据库 DEFAULT 生效
        sysMenuMapper.insertSelective(sysMenuDO);
        return MapstructUtils.convert(sysMenuDO, SysMenuBO.class);
    }

    public SysMenuBO update(SysMenuBO sysMenuBO) {
        SysMenuDO sysMenuDO = MapstructUtils.convert(sysMenuBO, SysMenuDO.class);
        sysMenuMapper.update(sysMenuDO);
        return MapstructUtils.convert(sysMenuDO, SysMenuBO.class);
    }

    public void deleteById(Long menuId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(SysMenuDO::getMenuId, menuId);
        sysMenuMapper.deleteByQuery(queryWrapper);
    }
}

