package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.bo.SysTenantBO;
import com.shiyu.ai.auth.repository.SysTenantRepository;
import com.shiyu.ai.auth.service.SysTenantService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 租户服务实现层
 *
 * @author shiyu-ai
 */
@Service
public class SysTenantServiceImpl implements SysTenantService {

    @Resource
    private SysTenantRepository sysTenantRepository;

    @Override
    public Pair<Long, List<SysTenantBO>> getAll(Number pageNumber, Number pageSize) {
        return sysTenantRepository.getAll(pageNumber, pageSize);
    }

    @Override
    public SysTenantBO getById(Long id) {
        return sysTenantRepository.getById(id);
    }

    @Override
    public SysTenantBO create(SysTenantBO sysTenantBO) {
        return sysTenantRepository.create(sysTenantBO);
    }

    @Override
    public SysTenantBO update(SysTenantBO sysTenantBO) {
        return sysTenantRepository.update(sysTenantBO);
    }

    @Override
    public void deleteById(Long id) {
        sysTenantRepository.deleteById(id);
    }
}
