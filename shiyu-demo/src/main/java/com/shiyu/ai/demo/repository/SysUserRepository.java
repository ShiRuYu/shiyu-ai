package com.shiyu.ai.demo.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.demo.domain.SysUserDO;
import com.shiyu.ai.demo.domain.bo.SysUserBO;
import com.shiyu.ai.demo.mapper.SysUserMapper;
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
}
