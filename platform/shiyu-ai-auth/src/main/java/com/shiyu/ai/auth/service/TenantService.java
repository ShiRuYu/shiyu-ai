package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.request.TenantRequest;
import com.shiyu.ai.auth.vo.TenantVO;
import com.shiyu.ai.common.core.api.PageData;

/** Tenant application contract. */
public interface TenantService {
    java.util.List<TenantVO> allTenantsView();
    TenantVO detailView(Long id);
    boolean createTenant(TenantRequest request);
    boolean updateTenant(Long id, TenantRequest request);
    PageData<TenantVO> getTenantPage(Number pageNo, Number pageSize,
                                     String name, String code, Integer status);
    boolean deleteTenant(Long id);
}
