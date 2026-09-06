package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.port.repository.TenantRepository;
import com.shiyu.ai.auth.service.TenantService;
import com.shiyu.ai.auth.request.TenantRequest;
import com.shiyu.ai.auth.vo.TenantVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.auth.vo.TenantVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.auth.domain.model.TenantBO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
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
    @Override public List<TenantVO> allTenantsView(ActorContext actor) { return MapstructUtils.convert(getAllTenants(requireActor(actor)), TenantVO.class); }
    @Override public TenantVO detailView(ActorContext actor, Long id) {
        ActorContext currentActor = requireActor(actor);
        TenantId targetTenantId = toTenantId(id);
        return MapstructUtils.convert(targetTenantId == null ? null : getTenantById(currentActor, targetTenantId), TenantVO.class);
    }
    @Override public boolean createTenant(ActorContext actor, TenantRequest request) { return createTenant(requireActor(actor), MapstructUtils.convert(request, TenantBO.class)); }
    @Override public boolean updateTenant(ActorContext actor, Long id, TenantRequest request) {
        ActorContext currentActor = requireActor(actor);
        TenantId targetTenantId = toTenantId(id);
        return targetTenantId != null && updateTenant(currentActor, targetTenantId, MapstructUtils.convert(request, TenantBO.class));
    }

    private final TenantRepository tenantRepository;

    public TenantServiceImpl(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public PageData<TenantVO> getTenantPage(ActorContext actor, Number pageNo, Number pageSize,
                                            String name, String code, Integer status) {
        requireActor(actor);
        var page = tenantRepository.selectPage(actor.tenantId(), pageNo, pageSize, name, code, status);
        return new PageData<>(MapstructUtils.convert(page.getRight(), TenantVO.class), page.getLeft());
    }

    private List<TenantBO> getAllTenants(ActorContext actor) {
        List<TenantBO> tenants = tenantRepository.selectAll();
        if (actor.platformAdmin() && actor.homeTenantId() == null) {
            return tenants;
        }
        Long currentTenantId = actor.tenantId().value();
        List<Long> visible = tenantRepository.selectDescendantIds(new TenantId(currentTenantId));
        return tenants.stream()
                .filter(item -> item.getId() != null && visible.contains(item.getId()))
                .toList();
    }

    private TenantBO getTenantById(ActorContext actor, TenantId id) {
        if (!canAccessTenant(actor, id)) {
            return null;
        }
        return tenantRepository.selectById(id.value());
    }

    @Transactional(rollbackFor = Exception.class)
    private boolean createTenant(ActorContext actor, TenantBO tenantBO) {
        log.info("新增租户，code: {}, name: {}", tenantBO.getCode(), tenantBO.getName());

        if (tenantRepository.existsByCode(tenantBO.getCode(), null)) {
            log.warn("租户编码已存在: {}", tenantBO.getCode());
            return false;
        }

        if (tenantBO.getStatus() == null) {
            tenantBO.setStatus(1);
        }

        Long currentTenantId = actor.tenantId().value();
        if (tenantBO.getParentId() == null) {
            tenantBO.setParentId(currentTenantId);
        } else if (!canAccessTenant(actor, toTenantId(tenantBO.getParentId()))) {
            log.warn("不能在当前作用域之外创建子租户，parentId={}", tenantBO.getParentId());
            return false;
        }

        tenantRepository.insert(tenantBO, actor.tenantId());
        return true;
    }

    private boolean updateTenant(ActorContext actor, TenantId id, TenantBO tenantBO) {
        log.info("修改租户，id: {}", id);

        TenantBO existing = tenantRepository.selectById(id.value());
        if (existing == null || !canAccessTenant(actor, id)) {
            return false;
        }

        if (tenantBO.getParentId() != null) {
            TenantId parentTenantId = toTenantId(tenantBO.getParentId());
            if (!canAccessTenant(actor, parentTenantId)
                    || tenantRepository.selectDescendantIds(id)
                            .contains(tenantBO.getParentId())) {
                return false;
            }
        }

        if (tenantBO.getCode() != null && !tenantBO.getCode().equals(existing.getCode())) {
            if (tenantRepository.existsByCode(tenantBO.getCode(), id.value())) {
                log.warn("租户编码已存在: {}", tenantBO.getCode());
                return false;
            }
        }

        tenantBO.setId(id.value());
        return tenantRepository.update(tenantBO);
    }

    @Override
    public boolean deleteTenant(ActorContext actor, Long id) {
        log.info("删除租户，id: {}", id);

        requireActor(actor);
        TenantId targetTenantId = toTenantId(id);
        if (targetTenantId == null || targetTenantId.value() == 1L || targetTenantId.equals(actor.tenantId())) {
            log.warn("禁止删除默认租户");
            return false;
        }

        if (!canAccessTenant(actor, targetTenantId)) {
            return false;
        }
        tenantRepository.cascadeDelete(targetTenantId);
        return true;
    }

    private List<TenantBO> getTenantTree(ActorContext actor) {
        List<TenantBO> allTenants = getAllTenants(actor);
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

    private boolean canAccessTenant(ActorContext actor, TenantId tenantId) {
        if (tenantId == null) {
            return false;
        }
        if (actor.platformAdmin() && actor.homeTenantId() == null) {
            return true;
        }
        return tenantRepository.selectDescendantIds(actor.tenantId()).contains(tenantId.value());
    }

    private TenantId toTenantId(Long value) {
        return value == null || value <= 0 ? null : new TenantId(value);
    }

    private ActorContext requireActor(ActorContext actor) {
        if (actor == null) {
            throw new IllegalArgumentException("actor context is required");
        }
        return actor;
    }
}
