package com.shiyu.ai.dal.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.shiyu.ai.dal.auth.dataobject.TenantQuotaDO;
import com.shiyu.ai.dal.auth.mapper.TenantQuotaMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TenantQuotaRepository {

    @Resource
    private TenantQuotaMapper tenantQuotaMapper;

    public TenantQuotaDO getByTenantId(Long tenantId) {
        return tenantQuotaMapper.selectOneByQuery(
            new QueryWrapper().eq("tenant_id", tenantId));
    }

    public void save(TenantQuotaDO quota) {
        TenantQuotaDO existing = getByTenantId(quota.getTenantId());
        if (existing != null) {
            quota.setId(existing.getId());
            tenantQuotaMapper.update(quota);
        } else {
            tenantQuotaMapper.insertSelective(quota);
        }
    }

    public List<TenantQuotaDO> listAll() {
        return tenantQuotaMapper.selectAll();
    }
}
