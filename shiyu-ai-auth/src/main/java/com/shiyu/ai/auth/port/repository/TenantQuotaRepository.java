package com.shiyu.ai.auth.port.repository;

import com.shiyu.ai.auth.domain.model.TenantQuotaBO;
import java.util.List;

public interface TenantQuotaRepository {
    TenantQuotaBO getByTenantId(Long tenantId);
    void save(TenantQuotaBO quota);
    List<TenantQuotaBO> listAll();
}
