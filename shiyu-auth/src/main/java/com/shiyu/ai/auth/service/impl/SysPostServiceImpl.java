package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.bo.SysPostBO;
import com.shiyu.ai.auth.repository.SysPostRepository;
import com.shiyu.ai.auth.service.SysPostService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 岗位服务实现层
 *
 * @author shiyu-ai
 */
@Service
public class SysPostServiceImpl implements SysPostService {

    @Resource
    private SysPostRepository sysPostRepository;

    @Override
    public Pair<Long, List<SysPostBO>> getAll(Number pageNumber, Number pageSize) {
        return sysPostRepository.getAll(pageNumber, pageSize);
    }

    @Override
    public SysPostBO getById(Long postId) {
        return sysPostRepository.getById(postId);
    }

    @Override
    public SysPostBO create(SysPostBO sysPostBO) {
        return sysPostRepository.create(sysPostBO);
    }

    @Override
    public SysPostBO update(SysPostBO sysPostBO) {
        return sysPostRepository.update(sysPostBO);
    }

    @Override
    public void deleteById(Long postId) {
        sysPostRepository.deleteById(postId);
    }
}
