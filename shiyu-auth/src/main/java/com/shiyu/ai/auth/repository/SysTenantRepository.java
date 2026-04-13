package com.shiyu.ai.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.auth.domain.SysTenantDO;
import com.shiyu.ai.auth.domain.bo.SysTenantBO;
import com.shiyu.ai.auth.mapper.SysTenantMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 租户数据仓储层
 *
 * @author shiyu-ai
 */
@Component
public class SysTenantRepository {

    @Resource
    private SysTenantMapper sysTenantMapper;

    public Pair<Long, List<SysTenantBO>> getAll(Number pageNumber, Number pageSize) {
        QueryWrapper queryWrapper = new QueryWrapper();
        List<SysTenantDO> sysTenants = sysTenantMapper.selectListByQuery(queryWrapper);
        long count = sysTenantMapper.selectCountByQuery(queryWrapper);
        return Pair.of(count, MapstructUtils.convert(sysTenants, SysTenantBO.class));
    }

    public SysTenantBO getById(Long id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(SysTenantDO::getId, id);
        SysTenantDO sysTenantDO = sysTenantMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(sysTenantDO, SysTenantBO.class);
    }

    public SysTenantBO create(SysTenantBO sysTenantBO) {
        SysTenantDO sysTenantDO = MapstructUtils.convert(sysTenantBO, SysTenantDO.class);
        
        // 使用 insertSelective 忽略 null 值，让数据库 DEFAULT 生效
        sysTenantMapper.insertSelective(sysTenantDO);
        return MapstructUtils.convert(sysTenantDO, SysTenantBO.class);
    }

    public SysTenantBO update(SysTenantBO sysTenantBO) {
        SysTenantDO sysTenantDO = MapstructUtils.convert(sysTenantBO, SysTenantDO.class);
        sysTenantMapper.update(sysTenantDO);
        return MapstructUtils.convert(sysTenantDO, SysTenantBO.class);
    }

    public void deleteById(Long id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(SysTenantDO::getId, id);
        sysTenantMapper.deleteByQuery(queryWrapper);
    }
}
