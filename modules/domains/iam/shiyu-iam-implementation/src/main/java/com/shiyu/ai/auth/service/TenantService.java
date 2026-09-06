package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.request.TenantRequest;
import com.shiyu.ai.auth.vo.TenantVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;

/** Tenant application contract. */
public interface TenantService {
    java.util.List<TenantVO> allTenantsView(ActorContext actor);
    TenantVO detailView(ActorContext actor, Long id);
    boolean createTenant(ActorContext actor, TenantRequest request);
    boolean updateTenant(ActorContext actor, Long id, TenantRequest request);
    PageData<TenantVO> getTenantPage(ActorContext actor, Number pageNo, Number pageSize,
                                     String name, String code, Integer status);
    boolean deleteTenant(ActorContext actor, Long id);
}
