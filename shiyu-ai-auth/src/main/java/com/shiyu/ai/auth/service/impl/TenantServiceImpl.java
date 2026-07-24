package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.dal.auth.repository.TenantRepository;
import com.shiyu.ai.auth.service.TenantService;
import com.shiyu.ai.dal.auth.bo.TenantBO;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    public TenantServiceImpl(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public List<TenantBO> getAllTenants() {
        List<TenantBO> tenants = tenantRepository.selectAll();
        if (LoginContextHolder.isSuperAdmin()) {
            return tenants;
        }
        List<Long> visible = LoginContextHolder.getVisibleTenantIds();
        if (visible == null) {
            return List.of();
        }
        return tenants.stream()
                .filter(item -> item.getId() != null && visible.contains(item.getId()))
                .toList();
    }

    @Override
    public TenantBO getTenantById(Long id) {
        if (!canAccessTenant(id)) {
            return null;
        }
        return tenantRepository.selectById(id);
    }

    @Override
    public boolean createTenant(TenantBO tenantBO) {
        log.info("新增租户，code: {}, name: {}", tenantBO.getCode(), tenantBO.getName());

        if (tenantRepository.existsByCode(tenantBO.getCode(), null)) {
            log.warn("租户编码已存在: {}", tenantBO.getCode());
            return false;
        }

        if (tenantBO.getStatus() == null) {
            tenantBO.setStatus(1);
        }

        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (!LoginContextHolder.isSuperAdmin()) {
            if (currentTenantId == null) {
                return false;
            }
            if (tenantBO.getParentId() == null) {
                tenantBO.setParentId(currentTenantId);
            } else if (!canAccessTenant(tenantBO.getParentId())) {
                log.warn("不能在当前作用域之外创建子租户，parentId={}", tenantBO.getParentId());
                return false;
            }
        }

        tenantRepository.insert(tenantBO);
        return true;
    }

    @Override
    public boolean updateTenant(Long id, TenantBO tenantBO) {
        log.info("修改租户，id: {}", id);

        TenantBO existing = tenantRepository.selectById(id);
        if (existing == null || !canAccessTenant(id)) {
            return false;
        }

        if (!LoginContextHolder.isSuperAdmin()
                && tenantBO.getParentId() != null
                && !canAccessTenant(tenantBO.getParentId())) {
            return false;
        }

        if (tenantBO.getCode() != null && !tenantBO.getCode().equals(existing.getCode())) {
            if (tenantRepository.existsByCode(tenantBO.getCode(), id)) {
                log.warn("租户编码已存在: {}", tenantBO.getCode());
                return false;
            }
        }

        tenantBO.setId(id);
        return tenantRepository.update(tenantBO);
    }

    @Override
    public boolean deleteTenant(Long id) {
        log.info("删除租户，id: {}", id);

        if (id == 1L || (!LoginContextHolder.isSuperAdmin()
                && id.equals(LoginContextHolder.getCurrentTenantId()))) {
            log.warn("禁止删除默认租户");
            return false;
        }

        if (!canAccessTenant(id)) {
            return false;
        }
        tenantRepository.cascadeDelete(id);
        return true;
    }

    private boolean canAccessTenant(Long tenantId) {
        if (tenantId == null) {
            return false;
        }
        if (LoginContextHolder.isSuperAdmin()) {
            return true;
        }
        List<Long> visible = LoginContextHolder.getVisibleTenantIds();
        return visible != null && visible.contains(tenantId);
    }
}
