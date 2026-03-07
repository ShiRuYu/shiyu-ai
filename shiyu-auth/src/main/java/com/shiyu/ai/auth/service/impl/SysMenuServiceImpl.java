package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.bo.SysMenuBO;
import com.shiyu.ai.auth.repository.SysMenuRepository;
import com.shiyu.ai.auth.service.SysMenuService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜单服务实现层
 *
 * @author shiyu-ai
 */
@Service
public class SysMenuServiceImpl implements SysMenuService {

    @Resource
    private SysMenuRepository sysMenuRepository;

    @Override
    public Pair<Long, List<SysMenuBO>> getAll(Number pageNumber, Number pageSize) {
        return sysMenuRepository.getAll(pageNumber, pageSize);
    }

    @Override
    public SysMenuBO getById(Long menuId) {
        return sysMenuRepository.getById(menuId);
    }

    @Override
    public SysMenuBO create(SysMenuBO sysMenuBO) {
        return sysMenuRepository.create(sysMenuBO);
    }

    @Override
    public SysMenuBO update(SysMenuBO sysMenuBO) {
        return sysMenuRepository.update(sysMenuBO);
    }

    @Override
    public void deleteById(Long menuId) {
        sysMenuRepository.deleteById(menuId);
    }
}
