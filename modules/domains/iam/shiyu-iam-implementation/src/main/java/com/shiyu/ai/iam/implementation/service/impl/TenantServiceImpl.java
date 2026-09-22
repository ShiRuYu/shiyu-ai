package com.shiyu.ai.iam.implementation.service.impl;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.iam.contract.module.TenantModuleAccessProvisioning;
import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import com.shiyu.ai.iam.implementation.port.repository.TenantRepository;
import com.shiyu.ai.iam.implementation.request.TenantRequest;
import com.shiyu.ai.iam.implementation.service.TenantService;
import com.shiyu.ai.iam.implementation.vo.TenantVO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提供 租户 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Slf4j
@Service
public class TenantServiceImpl implements TenantService {
    /**
     * 执行 租户 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<TenantVO> allTenantsView(ActorContext actor) {
        return MapstructUtils.convert(getAllTenants(requireActor(actor)), TenantVO.class);
    }

    /**
     * 查询 租户 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 租户 相关操作生成的结果数据。
     */
    @Override
    public TenantVO detailView(ActorContext actor, Long id) {
        ActorContext currentActor = requireActor(actor);
        TenantId targetTenantId = toTenantId(id);
        return MapstructUtils.convert(
                targetTenantId == null ? null : getTenantById(currentActor, targetTenantId),
                TenantVO.class);
    }

    /**
     * 执行 租户 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createTenant(ActorContext actor, TenantRequest request) {
        return createTenant(requireActor(actor), MapstructUtils.convert(request, TenantBO.class));
    }

    /**
     * 更新或设置 租户 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean updateTenant(ActorContext actor, Long id, TenantRequest request) {
        ActorContext currentActor = requireActor(actor);
        TenantId targetTenantId = toTenantId(id);
        return targetTenantId != null
                && updateTenant(
                        currentActor,
                        targetTenantId,
                        MapstructUtils.convert(request, TenantBO.class));
    }

    /**
     * 租户仓储，表示当前对象中的对应属性。
     */
    private final TenantRepository tenantRepository;
    private final TenantModuleAccessProvisioning tenantModuleAccessProvisioning;

    /**
     * 执行 租户 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantRepository 用于完成本次业务处理的 tenantRepository 参数。
     */
    public TenantServiceImpl(TenantRepository tenantRepository) {
        this(tenantRepository, tenantId -> {});
    }

    /**
     * 创建租户应用服务。
     *
     * @param tenantRepository 租户数据仓储
     * @param tenantModuleAccessProvisioning 模块默认状态初始化端口
     */
    @Autowired
    public TenantServiceImpl(
            TenantRepository tenantRepository,
            TenantModuleAccessProvisioning tenantModuleAccessProvisioning) {
        this.tenantRepository = tenantRepository;
        this.tenantModuleAccessProvisioning = tenantModuleAccessProvisioning;
    }

    /**
     * 查询 租户 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNo 分页页码，从 1 开始。
     * @param pageSize 每页返回的数据数量。
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param status 用于完成本次业务处理的 status 参数。
     * @return 返回 租户 相关操作生成的结果数据。
     */
    @Override
    public PageData<TenantVO> getTenantPage(
            ActorContext actor,
            Number pageNo,
            Number pageSize,
            String name,
            String code,
            Integer status) {
        requireActor(actor);
        var page =
                tenantRepository.selectPage(actor.tenantId(), pageNo, pageSize, name, code, status);
        return new PageData<>(
                MapstructUtils.convert(page.getRight(), TenantVO.class), page.getLeft());
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

    private boolean createTenant(ActorContext actor, TenantBO tenantBO) {
        log.info(
                "新增租户，codePresent: {}, namePresent: {}",
                tenantBO.getCode() != null,
                tenantBO.getName() != null);

        if (tenantRepository.existsByCode(tenantBO.getCode(), null)) {
            log.warn("租户编码已存在，codePresent={}", tenantBO.getCode() != null);
            return false;
        }

        if (tenantBO.getStatus() == null) {
            tenantBO.setStatus(1);
        }

        Long currentTenantId = actor.tenantId().value();
        if (tenantBO.getParentId() == null) {
            tenantBO.setParentId(currentTenantId);
        } else if (!canAccessTenant(actor, toTenantId(tenantBO.getParentId()))) {
            log.warn("不能在当前作用域之外创建子租户，parentIdPresent={}", tenantBO.getParentId() != null);
            return false;
        }

        TenantBO created = tenantRepository.insert(tenantBO, actor.tenantId());
        if (created == null || created.getId() == null) {
            throw new IllegalStateException("tenant insert did not return a persisted tenant");
        }
        TenantScope.withTenant(new TenantId(created.getId()), () -> {
            tenantRepository.initializeTenantSecurity(created, actor.tenantId());
            tenantModuleAccessProvisioning.initializeTenantDefaults(new TenantId(created.getId()));
            return null;
        });
        return true;
    }

    private boolean updateTenant(ActorContext actor, TenantId id, TenantBO tenantBO) {
        log.info("修改租户，tenantIdPresent: {}", id != null);

        TenantBO existing = tenantRepository.selectById(id.value());
        if (existing == null || !canAccessTenant(actor, id)) {
            return false;
        }

        if (tenantBO.getParentId() != null) {
            TenantId parentTenantId = toTenantId(tenantBO.getParentId());
            if (!canAccessTenant(actor, parentTenantId)
                    || tenantRepository.selectDescendantIds(id).contains(tenantBO.getParentId())) {
                return false;
            }
        }

        if (tenantBO.getCode() != null && !tenantBO.getCode().equals(existing.getCode())) {
            if (tenantRepository.existsByCode(tenantBO.getCode(), id.value())) {
                log.warn("租户编码已存在，codePresent={}", tenantBO.getCode() != null);
                return false;
            }
        }

        tenantBO.setId(id.value());
        return tenantRepository.update(tenantBO);
    }

    /**
     * 删除或移除 租户 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean deleteTenant(ActorContext actor, Long id) {
        log.info("删除租户，tenantIdPresent: {}", id != null);

        requireActor(actor);
        TenantId targetTenantId = toTenantId(id);
        if (targetTenantId == null
                || targetTenantId.value() == 1L
                || targetTenantId.equals(actor.tenantId())) {
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
