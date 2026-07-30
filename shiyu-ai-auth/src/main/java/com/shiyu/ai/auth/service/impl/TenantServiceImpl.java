package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.dal.auth.repository.TenantRepository;
import com.shiyu.ai.auth.service.TenantService;
import com.shiyu.ai.auth.vo.TenantVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.auth.bo.TenantBO;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    public TenantServiceImpl(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public PageData<TenantVO> getTenantPage(Number pageNo, Number pageSize,
                                            String name, String code, Integer status) {
        var page = tenantRepository.selectPage(pageNo, pageSize, name, code, status);
        return new PageData<>(MapstructUtils.convert(page.getRight(), TenantVO.class), page.getLeft());
    }

    @Override
    public List<TenantBO> getAllTenants() {
        List<TenantBO> tenants = tenantRepository.selectAll();
        if (LoginContextHolder.getUserId() == null) {
            return List.of();
        }
        if (LoginContextHolder.getHomeTenantId() == null
                && LoginContextHolder.isSuperAdmin()) {
            return tenants;
        }
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        List<Long> visible = currentTenantId == null
                ? List.of() : tenantRepository.selectDescendantIds(currentTenantId);
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
    @Transactional(rollbackFor = Exception.class)
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
        if (currentTenantId == null) {
            return false;
        }
        if (tenantBO.getParentId() == null) {
            tenantBO.setParentId(currentTenantId);
        } else if (!canAccessTenant(tenantBO.getParentId())) {
            log.warn("不能在当前作用域之外创建子租户，parentId={}", tenantBO.getParentId());
            return false;
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

        if (tenantBO.getParentId() != null) {
            if (!canAccessTenant(tenantBO.getParentId())
                    || tenantRepository.selectDescendantIds(id)
                            .contains(tenantBO.getParentId())) {
                return false;
            }
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

        if (id == 1L || id.equals(LoginContextHolder.getCurrentTenantId())) {
            log.warn("禁止删除默认租户");
            return false;
        }

        if (!canAccessTenant(id)) {
            return false;
        }
        tenantRepository.cascadeDelete(id);
        return true;
    }

    @Override
    public List<TenantBO> getTenantTree() {
        List<TenantBO> allTenants = getAllTenants();
        if (allTenants == null || allTenants.isEmpty()) {
            return new ArrayList<>();
        }

        // 一次遍历建立 parentId → children 映射
        Map<Long, List<TenantBO>> childrenMap = new HashMap<>();
        List<TenantBO> roots = new ArrayList<>();

        for (TenantBO tenant : allTenants) {
            Long pid = tenant.getParentId();
            if (pid == null) {
                roots.add(tenant);
            } else {
                childrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(tenant);
            }
        }

        // 递归挂载子节点
        for (TenantBO root : roots) {
            attachChildren(root, childrenMap);
        }
        return roots;
    }

    private void attachChildren(TenantBO parent, Map<Long, List<TenantBO>> childrenMap) {
        List<TenantBO> children = childrenMap.get(parent.getId());
        if (children != null && !children.isEmpty()) {
            parent.setChildren(children);
            for (TenantBO child : children) {
                attachChildren(child, childrenMap);
            }
        }
    }

    private boolean canAccessTenant(Long tenantId) {
        if (tenantId == null) {
            return false;
        }
        if (LoginContextHolder.getHomeTenantId() == null
                && LoginContextHolder.isSuperAdmin()) {
            return true;
        }
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        return currentTenantId != null
                && tenantRepository.selectDescendantIds(currentTenantId).contains(tenantId);
    }
}
