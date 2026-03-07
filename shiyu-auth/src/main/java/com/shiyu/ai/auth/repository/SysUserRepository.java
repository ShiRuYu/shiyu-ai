package com.shiyu.ai.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.auth.domain.SysUserDO;
import com.shiyu.ai.auth.domain.bo.SysUserBO;
import com.shiyu.ai.auth.mapper.SysUserMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户数据仓储层
 *
 * @author shiyu-ai
 */
@Component
public class SysUserRepository {

    @Resource
    private SysUserMapper sysUserMapper;

    public Pair<Long,List<SysUserBO>> getAll(Number pageNumber, Number pageSize) {
        QueryWrapper queryWrapper = new QueryWrapper();
        List<SysUserDO> sysUsers = sysUserMapper.selectListByQuery(queryWrapper);
        long count = sysUserMapper.selectCountByQuery(queryWrapper);
        return Pair.of(count, MapstructUtils.convert(sysUsers, SysUserBO.class));
    }

    public SysUserBO getById(Long userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(SysUserDO::getUserId, userId);
        SysUserDO sysUserDO = sysUserMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(sysUserDO, SysUserBO.class);
    }

    public SysUserBO create(SysUserBO sysUserBO) {
        SysUserDO sysUserDO = MapstructUtils.convert(sysUserBO, SysUserDO.class);
        sysUserMapper.insert(sysUserDO);
        return MapstructUtils.convert(sysUserDO, SysUserBO.class);
    }

    public SysUserBO update(SysUserBO sysUserBO) {
        SysUserDO sysUserDO = MapstructUtils.convert(sysUserBO, SysUserDO.class);
        sysUserMapper.update(sysUserDO);
        return MapstructUtils.convert(sysUserDO, SysUserBO.class);
    }

    public void deleteById(Long userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(SysUserDO::getUserId, userId);
        sysUserMapper.deleteByQuery(queryWrapper);
    }
}
