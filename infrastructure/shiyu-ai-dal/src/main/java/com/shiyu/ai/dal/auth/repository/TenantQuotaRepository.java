package com.shiyu.ai.dal.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.shiyu.ai.auth.domain.model.TenantQuotaBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.auth.dataobject.TenantQuotaDO;
import com.shiyu.ai.dal.auth.mapper.TenantQuotaMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TenantQuotaRepository implements com.shiyu.ai.auth.port.repository.TenantQuotaRepository {

    @Resource
    private TenantQuotaMapper tenantQuotaMapper;

    public TenantQuotaBO getByTenantId(Long tenantId) {
        return MapstructUtils.convert(tenantQuotaMapper.selectOneByQuery(
            new QueryWrapper().eq("tenant_id", tenantId)), TenantQuotaBO.class);
    }

    public void save(TenantQuotaBO quota) {
        TenantQuotaDO existing = tenantQuotaMapper.selectOneByQuery(
                new QueryWrapper().eq("tenant_id", quota.getTenantId()));
        TenantQuotaDO data = MapstructUtils.convert(quota, TenantQuotaDO.class);
        if (existing != null) {
            data.setId(existing.getId());
            tenantQuotaMapper.update(data);
        } else {
            tenantQuotaMapper.insertSelective(data);
        }
        quota.setId(data.getId());
    }

    public List<TenantQuotaBO> listAll() {
        return MapstructUtils.convert(tenantQuotaMapper.selectAll(), TenantQuotaBO.class);
    }
}
