package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.bo.SysRoleBO;
import com.shiyu.ai.auth.repository.SysRoleRepository;
import com.shiyu.ai.auth.service.SysRoleService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色服务实现层
 *
 * @author shiyu-ai
 */
@Service
public class SysRoleServiceImpl implements SysRoleService {

    @Resource
    private SysRoleRepository sysRoleRepository;

    @Override
    public Pair<Long, List<SysRoleBO>> getAll(Number pageNumber, Number pageSize) {
        return sysRoleRepository.getAll(pageNumber, pageSize);
    }

    @Override
    public SysRoleBO getById(Long roleId) {
        return sysRoleRepository.getById(roleId);
    }

    @Override
    public SysRoleBO create(SysRoleBO sysRoleBO) {
        return sysRoleRepository.create(sysRoleBO);
    }

    @Override
    public SysRoleBO update(SysRoleBO sysRoleBO) {
        return sysRoleRepository.update(sysRoleBO);
    }

    @Override
    public void deleteById(Long roleId) {
        sysRoleRepository.deleteById(roleId);
    }
}
