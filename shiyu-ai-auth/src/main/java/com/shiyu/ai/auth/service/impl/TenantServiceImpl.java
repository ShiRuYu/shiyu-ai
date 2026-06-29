package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.repository.TenantRepository;
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
        log.info("鏂板绉熸埛锛宑ode: {}, name: {}", tenantBO.getCode(), tenantBO.getName());

        if (tenantRepository.existsByCode(tenantBO.getCode(), null)) {
            log.warn("绉熸埛缂栫爜宸插瓨鍦? {}", tenantBO.getCode());
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
        log.info("淇敼绉熸埛锛宨d: {}", id);

        TenantBO existing = tenantRepository.selectById(id);
        if (existing == null) {
            return false;
        }

        if (tenantBO.getCode() != null && !tenantBO.getCode().equals(existing.getCode())) {
            if (tenantRepository.existsByCode(tenantBO.getCode(), id)) {
                log.warn("绉熸埛缂栫爜宸插瓨鍦? {}", tenantBO.getCode());
                return false;
            }
        }

        tenantBO.setId(id);
        return tenantRepository.update(tenantBO);
    }

    @Override
    public boolean deleteTenant(Long id) {
        log.info("鍒犻櫎绉熸埛锛宨d: {}", id);

        if (id == 1L) {
            log.warn("绂佹鍒犻櫎榛樿绉熸埛");
            return false;
        }

        return tenantRepository.deleteById(id);
    }
}
