package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.bo.SysDeptBO;
import com.shiyu.ai.auth.repository.SysDeptRepository;
import com.shiyu.ai.auth.service.SysDeptService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部门服务实现层
 *
 * @author shiyu-ai
 */
@Service
public class SysDeptServiceImpl implements SysDeptService {

    @Resource
    private SysDeptRepository sysDeptRepository;

    @Override
    public Pair<Long, List<SysDeptBO>> getAll(Number pageNumber, Number pageSize) {
        return sysDeptRepository.getAll(pageNumber, pageSize);
    }

    @Override
    public SysDeptBO getById(Long deptId) {
        return sysDeptRepository.getById(deptId);
    }

    @Override
    public SysDeptBO create(SysDeptBO sysDeptBO) {
        return sysDeptRepository.create(sysDeptBO);
    }

    @Override
    public SysDeptBO update(SysDeptBO sysDeptBO) {
        return sysDeptRepository.update(sysDeptBO);
    }

    @Override
    public void deleteById(Long deptId) {
        sysDeptRepository.deleteById(deptId);
    }
}
