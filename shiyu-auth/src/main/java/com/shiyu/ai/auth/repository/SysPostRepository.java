package com.shiyu.ai.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.auth.domain.SysPostDO;
import com.shiyu.ai.auth.domain.bo.SysPostBO;
import com.shiyu.ai.auth.mapper.SysPostMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 岗位数据仓储层
 *
 * @author shiyu-ai
 */
@Component
public class SysPostRepository {

    @Resource
    private SysPostMapper sysPostMapper;

    public Pair<Long, List<SysPostBO>> getAll(Number pageNumber, Number pageSize) {
        QueryWrapper queryWrapper = new QueryWrapper();
        List<SysPostDO> sysPosts = sysPostMapper.selectListByQuery(queryWrapper);
        long count = sysPostMapper.selectCountByQuery(queryWrapper);
        return Pair.of(count, MapstructUtils.convert(sysPosts, SysPostBO.class));
    }

    public SysPostBO getById(Long postId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(SysPostDO::getPostId, postId);
        SysPostDO sysPostDO = sysPostMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(sysPostDO, SysPostBO.class);
    }

    public SysPostBO create(SysPostBO sysPostBO) {
        SysPostDO sysPostDO = MapstructUtils.convert(sysPostBO, SysPostDO.class);
        
        // 使用 insertSelective 忽略 null 值，让数据库 DEFAULT 生效
        sysPostMapper.insertSelective(sysPostDO);
        return MapstructUtils.convert(sysPostDO, SysPostBO.class);
    }

    public SysPostBO update(SysPostBO sysPostBO) {
        SysPostDO sysPostDO = MapstructUtils.convert(sysPostBO, SysPostDO.class);
        sysPostMapper.update(sysPostDO);
        return MapstructUtils.convert(sysPostDO, SysPostBO.class);
    }

    public void deleteById(Long postId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(SysPostDO::getPostId, postId);
        sysPostMapper.deleteByQuery(queryWrapper);
    }
}
