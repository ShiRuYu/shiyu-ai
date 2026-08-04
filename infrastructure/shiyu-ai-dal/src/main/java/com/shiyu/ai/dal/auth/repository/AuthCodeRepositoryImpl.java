package com.shiyu.ai.dal.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.auth.domain.model.AuthCodeBO;
import com.shiyu.ai.auth.domain.model.RoleScopeAuthCodeBO;
import com.shiyu.ai.auth.domain.model.TenantAuthCodeBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.auth.dataobject.AuthCodeDO;
import com.shiyu.ai.dal.auth.dataobject.RoleScopeAuthCodeDO;
import com.shiyu.ai.dal.auth.dataobject.TenantAuthCodeDO;
import com.shiyu.ai.dal.auth.mapper.AuthCodeMapper;
import com.shiyu.ai.dal.auth.mapper.RoleScopeAuthCodeMapper;
import com.shiyu.ai.dal.auth.mapper.TenantAuthCodeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/** Embedded H2 implementation of the authorization-code port. */
@Component
public class AuthCodeRepositoryImpl implements com.shiyu.ai.auth.port.repository.AuthCodeRepository {

    @Resource
    private AuthCodeMapper authCodeMapper;
    @Resource
    private RoleScopeAuthCodeMapper roleScopeAuthCodeMapper;
    @Resource
    private TenantAuthCodeMapper tenantAuthCodeMapper;

    @Override
    public List<AuthCodeBO> selectByTenantId(Long tenantId) {
        List<Long> ids = tenantAuthCodeMapper.selectListByQuery(QueryWrapper.create()
                        .eq(TenantAuthCodeDO::getTenantId, tenantId)
                        .eq(TenantAuthCodeDO::getStatus, 1))
                .stream().map(TenantAuthCodeDO::getAuthCodeId).toList();
        if (ids.isEmpty()) return List.of();
        return MapstructUtils.convert(authCodeMapper.selectListByQuery(QueryWrapper.create()
                .in(AuthCodeDO::getId, ids)
                .eq(AuthCodeDO::getStatus, 1)
                .eq(AuthCodeDO::getDelFlag, 0)), AuthCodeBO.class);
    }

    @Override
    public List<AuthCodeBO> selectByRoleIdAndTenantId(Long roleId, Long tenantId) {
        List<Long> ids = roleScopeAuthCodeMapper.selectListByQuery(QueryWrapper.create()
                        .eq(RoleScopeAuthCodeDO::getRoleId, roleId)
                        .eq(RoleScopeAuthCodeDO::getTenantId, tenantId)
                        .eq(RoleScopeAuthCodeDO::getStatus, 1)
                        .eq(RoleScopeAuthCodeDO::getDelFlag, 0))
                .stream().map(RoleScopeAuthCodeDO::getAuthCodeId).toList();
        if (ids.isEmpty()) return List.of();
        return MapstructUtils.convert(authCodeMapper.selectListByQuery(QueryWrapper.create()
                .in(AuthCodeDO::getId, ids)
                .eq(AuthCodeDO::getStatus, 1)
                .eq(AuthCodeDO::getDelFlag, 0)), AuthCodeBO.class);
    }

    @Override
    public AuthCodeBO selectById(Long id) {
        return MapstructUtils.convert(authCodeMapper.selectOneById(id), AuthCodeBO.class);
    }

    @Override
    public AuthCodeBO insert(AuthCodeBO code) {
        AuthCodeDO data = MapstructUtils.convert(code, AuthCodeDO.class);
        authCodeMapper.insertSelective(data);
        return MapstructUtils.convert(data, AuthCodeBO.class);
    }

    @Override
    public void update(AuthCodeBO code) {
        authCodeMapper.update(MapstructUtils.convert(code, AuthCodeDO.class));
    }

    @Override
    public boolean existsByCode(String code, Long excludeId) {
        QueryWrapper query = QueryWrapper.create().eq(AuthCodeDO::getCode, code).eq(AuthCodeDO::getDelFlag, 0);
        if (excludeId != null) query.ne(AuthCodeDO::getId, excludeId);
        return authCodeMapper.selectCountByQuery(query) > 0;
    }

    @Override
    public boolean isAvailable(Long authCodeId, Long tenantId) {
        return tenantAuthCodeMapper.selectCountByQuery(QueryWrapper.create()
                .eq(TenantAuthCodeDO::getTenantId, tenantId)
                .eq(TenantAuthCodeDO::getAuthCodeId, authCodeId)
                .eq(TenantAuthCodeDO::getStatus, 1)) > 0;
    }

    @Override
    public List<AuthCodeBO> selectAvailableByIds(List<Long> ids, Long tenantId) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<Long> available = tenantAuthCodeMapper.selectListByQuery(QueryWrapper.create()
                        .eq(TenantAuthCodeDO::getTenantId, tenantId)
                        .eq(TenantAuthCodeDO::getStatus, 1)
                        .in(TenantAuthCodeDO::getAuthCodeId, ids))
                .stream().map(TenantAuthCodeDO::getAuthCodeId).toList();
        if (available.isEmpty()) return List.of();
        return MapstructUtils.convert(authCodeMapper.selectListByQuery(QueryWrapper.create()
                .in(AuthCodeDO::getId, available)
                .eq(AuthCodeDO::getStatus, 1)
                .eq(AuthCodeDO::getDelFlag, 0)), AuthCodeBO.class);
    }

    @Override
    public boolean hasRoleAssignments(Long authCodeId) {
        return roleScopeAuthCodeMapper.selectCountByQuery(QueryWrapper.create()
                .eq(RoleScopeAuthCodeDO::getAuthCodeId, authCodeId)
                .eq(RoleScopeAuthCodeDO::getDelFlag, 0)) > 0;
    }

    @Override
    public void insertTenantCode(TenantAuthCodeBO assignment) {
        tenantAuthCodeMapper.insertSelective(MapstructUtils.convert(assignment, TenantAuthCodeDO.class));
    }

    @Override
    public void deleteTenantCode(Long tenantId, Long authCodeId) {
        tenantAuthCodeMapper.deleteByQuery(QueryWrapper.create()
                .eq(TenantAuthCodeDO::getTenantId, tenantId)
                .eq(TenantAuthCodeDO::getAuthCodeId, authCodeId));
    }

    @Override
    public long countActiveTenantLinks(Long authCodeId) {
        return tenantAuthCodeMapper.selectCountByQuery(QueryWrapper.create()
                .eq(TenantAuthCodeDO::getAuthCodeId, authCodeId)
                .eq(TenantAuthCodeDO::getStatus, 1));
    }

    @Override
    public void insertRoleAssignments(List<RoleScopeAuthCodeBO> assignments) {
        if (assignments == null || assignments.isEmpty()) return;
        roleScopeAuthCodeMapper.insertBatch(assignments.stream()
                .map(item -> MapstructUtils.convert(item, RoleScopeAuthCodeDO.class)).toList());
    }

    @Override
    public void deleteRoleAssignments(Long roleId, Long tenantId, Long authCodeId) {
        QueryWrapper query = QueryWrapper.create()
                .eq(RoleScopeAuthCodeDO::getRoleId, roleId)
                .eq(RoleScopeAuthCodeDO::getTenantId, tenantId);
        if (authCodeId != null) query.eq(RoleScopeAuthCodeDO::getAuthCodeId, authCodeId);
        roleScopeAuthCodeMapper.deleteByQuery(query);
    }
}
