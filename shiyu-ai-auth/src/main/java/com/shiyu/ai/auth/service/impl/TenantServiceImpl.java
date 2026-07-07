package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.dal.repository.auth.TenantRepository;
import com.shiyu.ai.auth.service.TenantService;
import com.shiyu.ai.auth.bo.TenantBO;
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
        return tenantRepository.selectAll();
    }

    @Override
    public TenantBO getTenantById(Long id) {
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
            tenantBO.setStatus("1");
        }

        tenantRepository.insert(tenantBO);
        return true;
    }

    @Override
    public boolean updateTenant(Long id, TenantBO tenantBO) {
        log.info("修改租户，id: {}", id);

        TenantBO existing = tenantRepository.selectById(id);
        if (existing == null) {
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

        if (id == 1L) {
            log.warn("禁止删除默认租户");
            return false;
        }

        return tenantRepository.deleteById(id);
    }
}
