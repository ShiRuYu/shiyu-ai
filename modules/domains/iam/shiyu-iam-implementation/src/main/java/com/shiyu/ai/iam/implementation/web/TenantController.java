package com.shiyu.ai.iam.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
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
 * {@code TenantController} 是Web模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
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
     * {@code getAllTenants} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("system:tenant:list")
    @GetMapping("/list")
    public Result<List<TenantVO>> getAllTenants() {
        return Result.success(tenantService.allTenantsView(ActorContextHttpAdapter.currentActor()));
    }

    /**
     * {@code getTenantPage} 查询并返回当前操作所需的数据。
     *
     * @param r 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("system:tenant:list")
    @GetMapping("/page")
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
     * {@code getTenantById} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("system:tenant:list")
    @GetMapping("/detail")
    public Result<TenantVO> getTenantById(@RequestParam Long id) {
        var v = tenantService.detailView(ActorContextHttpAdapter.currentActor(), id);
        return v == null ? Result.fail("租户不存在") : Result.success(v);
    }

    /**
     * {@code createTenant} 写入或更新当前模块中的业务数据。
     *
     * @param r 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("system:tenant:create")
    @PostMapping("/create")
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
     * {@code updateTenant} 写入或更新当前模块中的业务数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param r 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("system:tenant:update")
    @PostMapping("/update")
    public Result<Void> updateTenant(@RequestParam Long id, @Valid @RequestBody TenantRequest r) {
        return tenantService.updateTenant(ActorContextHttpAdapter.currentActor(), id, r)
                ? Result.success()
                : Result.fail("修改失败");
    }

    /**
     * {@code deleteTenant} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("system:tenant:delete")
    @PostMapping("/delete")
    public Result<Void> deleteTenant(@RequestParam Long id) {
        return tenantService.deleteTenant(ActorContextHttpAdapter.currentActor(), id)
                ? Result.success()
                : Result.fail("删除失败");
    }
}
