package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.bo.SysUserBO;
import com.shiyu.ai.auth.repository.SysUserRepository;
import com.shiyu.ai.auth.service.SysUserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现层
 *
 * @author shiyu-ai
 */
@Service
public class SysUserServiceImpl implements SysUserService {

    @Resource
    private SysUserRepository sysUserRepository;

    @Override
    public Pair<Long, List<SysUserBO>> getAll(Number pageNumber, Number pageSize) {
        return sysUserRepository.getAll(pageNumber, pageSize);
    }

    @Override
    public SysUserBO getById(Long userId) {
        return sysUserRepository.getById(userId);
    }

    @Override
    public SysUserBO create(SysUserBO sysUserBO) {
        return sysUserRepository.create(sysUserBO);
    }

    @Override
    public SysUserBO update(SysUserBO sysUserBO) {
        return sysUserRepository.update(sysUserBO);
    }

    @Override
    public void deleteById(Long userId) {
        sysUserRepository.deleteById(userId);
    }
}
