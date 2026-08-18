package com.shiyu.ai.web.auth;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.auth.request.TenantPageRequest;
import com.shiyu.ai.auth.request.TenantRequest;
import com.shiyu.ai.auth.service.AuthService;
import com.shiyu.ai.auth.service.TenantService;
import com.shiyu.ai.auth.vo.TenantVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/system/tenants")
@RequiredArgsConstructor
public class TenantController {
    private final TenantService tenantService;
    private final AuthService authService;
    private final KnowledgeSpaceService knowledgeSpaceService;
    @SaCheckPermission("system:tenant:list") @GetMapping("/list")
    public Result<List<TenantVO>> getAllTenants() { return Result.success(tenantService.allTenantsView()); }
    @SaCheckPermission("system:tenant:list") @GetMapping("/page")
    public Result<PageData<TenantVO>> getTenantPage(@Valid TenantPageRequest r) { return Result.success(tenantService.getTenantPage(r.getPageNum(),r.getPageSize(),r.getName(),r.getCode(),r.getStatus())); }
    @SaCheckPermission("system:tenant:list") @GetMapping("/detail")
    public Result<TenantVO> getTenantById(@RequestParam Long id) { var v=tenantService.detailView(id); return v==null?Result.fail("租户不存在"):Result.success(v); }
    @SaCheckPermission("system:tenant:create") @PostMapping("/create")
    public Result<Void> createTenant(@Valid @RequestBody TenantRequest r) { boolean ok=tenantService.createTenant(r); if(ok){ var all=tenantService.allTenantsView(); all.stream().filter(t->r.getCode()!=null&&r.getCode().equals(t.getCode())).findFirst().ifPresent(t->knowledgeSpaceService.initializeTenantDefaults(t.getId())); return Result.success(); } return Result.fail("新增失败"); }
    @SaCheckPermission("system:tenant:update") @PostMapping("/update")
    public Result<Void> updateTenant(@RequestParam Long id,@Valid @RequestBody TenantRequest r) { return tenantService.updateTenant(id,r)?Result.success():Result.fail("修改失败"); }
    @SaCheckPermission("system:tenant:delete") @PostMapping("/delete")
    public Result<Void> deleteTenant(@RequestParam Long id) { return tenantService.deleteTenant(id)?Result.success():Result.fail("删除失败"); }
}
