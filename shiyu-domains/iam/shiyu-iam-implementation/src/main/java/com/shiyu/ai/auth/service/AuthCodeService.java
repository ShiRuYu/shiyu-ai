package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.api.request.AuthCodeRequest;
import com.shiyu.ai.auth.api.response.AuthCodeResponse;
import com.shiyu.ai.auth.request.AuthCodePageRequest;
import com.shiyu.ai.auth.vo.AuthCodeOptionVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/** Authorization-code application contract. */
public interface AuthCodeService {
    List<AuthCodeOptionVO> list(ActorContext actor);
    List<String> listRoleAuthCodes(ActorContext actor, Long roleId, TenantId tenantId);
    List<AuthCodeOptionVO> options(ActorContext actor);
    AuthCodeResponse create(ActorContext actor, AuthCodeRequest request);
    boolean update(ActorContext actor, Long id, AuthCodeRequest request);
    boolean delete(ActorContext actor, Long id);
    boolean grant(ActorContext actor, Long roleId, TenantId tenantId, List<Long> authCodeIds);
    boolean replace(ActorContext actor, Long roleId, TenantId tenantId, List<String> authCodes);
    boolean revoke(ActorContext actor, Long roleId, TenantId tenantId, Long authCodeId);
    PageData<AuthCodeOptionVO> page(ActorContext actor, AuthCodePageRequest request);
}
