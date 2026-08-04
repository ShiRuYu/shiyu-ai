package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.api.request.AuthCodeRequest;
import com.shiyu.ai.auth.api.response.AuthCodeResponse;
import com.shiyu.ai.auth.request.AuthCodePageRequest;
import com.shiyu.ai.auth.vo.AuthCodeOptionVO;
import com.shiyu.ai.common.core.api.PageData;

import java.util.List;

/** Authorization-code application contract. */
public interface AuthCodeService {
    List<AuthCodeOptionVO> list();
    List<String> listRoleAuthCodes(Long roleId, Long tenantId);
    List<AuthCodeOptionVO> options();
    AuthCodeResponse create(AuthCodeRequest request);
    boolean update(Long id, AuthCodeRequest request);
    boolean delete(Long id);
    boolean grant(Long roleId, Long tenantId, List<Long> authCodeIds);
    boolean replace(Long roleId, Long tenantId, List<String> authCodes);
    boolean revoke(Long roleId, Long tenantId, Long authCodeId);
    PageData<AuthCodeOptionVO> page(AuthCodePageRequest request);
}
