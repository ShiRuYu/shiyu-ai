package com.shiyu.ai.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.auth.domain.SysDeptDO;
import com.shiyu.ai.auth.domain.bo.SysDeptBO;
import com.shiyu.ai.auth.mapper.SysDeptMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 部门数据仓储�?
 *
 * @author shiyu-ai
 */
@Component
@Transactional
public class SysDeptRepository {

    @Resource
    private SysDeptMapper sysDeptMapper;

    public Pair<Long, List<SysDeptBO>> getAll(Integer pageNumber, Integer pageSize) {
        QueryWrapper queryWrapper = new QueryWrapper();
        long count = sysDeptMapper.selectCountByQuery(queryWrapper);
        if (pageNumber != null && pageSize != null && pageSize > 0) {
            queryWrapper.limit((pageNumber.longValue() - 1) * pageSize.longValue(), pageSize);
        }
        List<SysDeptDO> sysDepts = sysDeptMapper.selectListByQuery(queryWrapper);
        return Pair.of(count, MapstructUtils.convert(sysDepts, SysDeptBO.class));
    }

    public SysDeptBO getById(Long deptId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(SysDeptDO::getDeptId, deptId);
        SysDeptDO sysDeptDO = sysDeptMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(sysDeptDO, SysDeptBO.class);
    }

    public SysDeptBO create(SysDeptBO sysDeptBO) {
        SysDeptDO sysDeptDO = MapstructUtils.convert(sysDeptBO, SysDeptDO.class);
        
        // 使用 insertSelective 忽略 null 值，让数据库 DEFAULT 生效
        sysDeptMapper.insertSelective(sysDeptDO);
        return MapstructUtils.convert(sysDeptDO, SysDeptBO.class);
    }

    public SysDeptBO update(SysDeptBO sysDeptBO) {
        SysDeptDO sysDeptDO = MapstructUtils.convert(sysDeptBO, SysDeptDO.class);
        sysDeptMapper.update(sysDeptDO);
        return MapstructUtils.convert(sysDeptDO, SysDeptBO.class);
    }

    public void deleteById(Long deptId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(SysDeptDO::getDeptId, deptId);
        sysDeptMapper.deleteByQuery(queryWrapper);
    }
}

