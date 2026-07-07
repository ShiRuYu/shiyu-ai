package com.shiyu.ai.dal.repository.auth;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.auth.TenantDO;
import com.shiyu.ai.dal.mapper.auth.TenantMapper;
import com.shiyu.ai.auth.bo.TenantBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TenantRepository {

    private final TenantMapper tenantMapper;

    public TenantRepository(TenantMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }

    public Pair<Long, List<TenantBO>> selectPage(Number pageNo, Number pageSize, String name) {
        QueryWrapper countWrapper = new QueryWrapper();
        if (name != null && !name.isEmpty()) {
            countWrapper.like(TenantDO::getName, name);
        }
        long count = tenantMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        if (name != null && !name.isEmpty()) {
            queryWrapper.like(TenantDO::getName, name);
        }
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }
        queryWrapper.orderBy(TenantDO::getId, true);

        List<TenantDO> tenantDOs = tenantMapper.selectListByQuery(queryWrapper);
        return Pair.of(count, MapstructUtils.convert(tenantDOs, TenantBO.class));
    }

    public List<TenantBO> selectAll() {
        List<TenantDO> tenantDOs = tenantMapper.selectAll();
        return MapstructUtils.convert(tenantDOs, TenantBO.class);
    }

    public TenantBO selectById(Long id) {
        TenantDO tenantDO = tenantMapper.selectOneById(id);
        return MapstructUtils.convert(tenantDO, TenantBO.class);
    }

    public TenantBO insert(TenantBO tenantBO) {
        TenantDO tenantDO = MapstructUtils.convert(tenantBO, TenantDO.class);
        tenantMapper.insertSelective(tenantDO);
        tenantBO.setId(tenantDO.getId());
        return tenantBO;
    }

    public boolean update(TenantBO tenantBO) {
        TenantDO tenantDO = MapstructUtils.convert(tenantBO, TenantDO.class);
        return tenantMapper.update(tenantDO) > 0;
    }

    public boolean deleteById(Long id) {
        return tenantMapper.deleteById(id) > 0;
    }

    public boolean existsByCode(String code, Long excludeId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(TenantDO::getCode, code);
        if (excludeId != null) {
            qw.ne(TenantDO::getId, excludeId);
        }
        return tenantMapper.selectCountByQuery(qw) > 0;
    }
}
