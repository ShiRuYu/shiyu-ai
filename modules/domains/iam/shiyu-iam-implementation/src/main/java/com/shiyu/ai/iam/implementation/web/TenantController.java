package com.shiyu.ai.iam.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.iam.implementation.request.TenantPageRequest;
import com.shiyu.ai.iam.implementation.request.TenantRequest;
import com.shiyu.ai.iam.implementation.service.AuthService;
import com.shiyu.ai.iam.implementation.service.TenantService;
import com.shiyu.ai.iam.implementation.vo.TenantVO;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.contract.KnowledgeTenantProvisioning;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 处理 租户 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/iam/tenants")
@RequiredArgsConstructor
public class TenantController {
    /**
     * 租户服务，表示当前对象中的对应属性。
     */
    private final TenantService tenantService;
    /**
     * authService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AuthService authService;
    /**
     * knowledgeSpaceService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeTenantProvisioning knowledgeSpaceService;

    /**
     * 执行 租户 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     */
    @SaCheckPermission("system:tenant:list")
    @GetMapping("/tree")
    public Result<List<TenantVO>> getAllTenants() {
        return Result.success(tenantService.allTenantsView(ActorContextHttpAdapter.currentActor()));
    }

    /**
     * 执行 租户 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     */
    @SaCheckPermission("system:tenant:list")
    @GetMapping
    public Result<PageData<TenantVO>> getTenantPage(@Valid TenantPageRequest r) {
        return Result.success(
                tenantService.getTenantPage(
                        ActorContextHttpAdapter.currentActor(),
                        r.getPageNum(),
                        r.getPageSize(),
                        r.getName(),
                        r.getCode(),
                        r.getStatus()));
    }

    /**
     * 执行 租户 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     */
    @SaCheckPermission("system:tenant:list")
    @GetMapping("/{id}")
    public Result<TenantVO> getTenantById(@PathVariable Long id) {
        var v = tenantService.detailView(ActorContextHttpAdapter.currentActor(), id);
        return v == null ? Result.fail("租户不存在") : Result.success(v);
    }

    /**
     * 执行 租户 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param create 用于完成本次业务处理的 create 参数。
     */
    @SaCheckPermission("system:tenant:create")
    @PostMapping
    public Result<Void> createTenant(@Valid @RequestBody TenantRequest r) {
        var actor = ActorContextHttpAdapter.currentActor();
        boolean ok = tenantService.createTenant(actor, r);
        if (ok) {
            var all = tenantService.allTenantsView(actor);
            all.stream()
                    .filter(t -> r.getCode() != null && r.getCode().equals(t.getCode()))
                    .findFirst()
                    .ifPresent(
                            t ->
                                    knowledgeSpaceService.initializeTenantDefaults(
                                            new TenantId(t.getId())));
            return Result.success();
        }
        return Result.fail("新增失败");
    }

    /**
     * 执行 租户 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param update 用于完成本次业务处理的 update 参数。
     */
    @SaCheckPermission("system:tenant:update")
    @PutMapping("/{id}")
    public Result<Void> updateTenant(@PathVariable Long id, @Valid @RequestBody TenantRequest r) {
        return tenantService.updateTenant(ActorContextHttpAdapter.currentActor(), id, r)
                ? Result.success()
                : Result.fail("修改失败");
    }

    /**
     * 执行 租户 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param delete 用于完成本次业务处理的 delete 参数。
     */
    @SaCheckPermission("system:tenant:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTenant(@PathVariable Long id) {
        return tenantService.deleteTenant(ActorContextHttpAdapter.currentActor(), id)
                ? Result.success()
                : Result.fail("删除失败");
    }
}
