package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.bo.SysWorkspaceBO;
import com.shiyu.ai.auth.repository.SysWorkspaceRepository;
import com.shiyu.ai.auth.service.SysWorkspaceService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 工作空间服务实现层
 *
 * @author shiyu-ai
 */
@Service
public class SysWorkspaceServiceImpl implements SysWorkspaceService {

    @Resource
    private SysWorkspaceRepository sysWorkspaceRepository;

    @Override
    public Pair<Long, List<SysWorkspaceBO>> getAll(Number pageNumber, Number pageSize) {
        return sysWorkspaceRepository.getAll(pageNumber, pageSize);
    }

    @Override
    public SysWorkspaceBO getById(Long workspaceId) {
        return sysWorkspaceRepository.getById(workspaceId);
    }

    @Override
    public SysWorkspaceBO create(SysWorkspaceBO sysWorkspaceBO) {
        return sysWorkspaceRepository.create(sysWorkspaceBO);
    }

    @Override
    public SysWorkspaceBO update(SysWorkspaceBO sysWorkspaceBO) {
        return sysWorkspaceRepository.update(sysWorkspaceBO);
    }

    @Override
    public void deleteById(Long workspaceId) {
        sysWorkspaceRepository.deleteById(workspaceId);
    }
}
