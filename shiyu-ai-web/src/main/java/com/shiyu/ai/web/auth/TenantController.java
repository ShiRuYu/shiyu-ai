package com.shiyu.ai.web.auth;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.auth.service.AuthService;
import com.shiyu.ai.auth.service.TenantService;
import com.shiyu.ai.dal.auth.bo.TenantBO;
import com.shiyu.ai.auth.request.TenantRequest;
import com.shiyu.ai.auth.vo.TenantVO;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.auth.request.TenantPageRequest;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@Slf4j
@Tag(name = "Tenant", description = "Tenant")
@RestController
@RequestMapping("/tenant")
public class TenantController {

    private final TenantService tenantService;
    private final AuthService authService;
    private final KnowledgeSpaceService knowledgeSpaceService;

    public TenantController(TenantService tenantService, AuthService authService,
                            KnowledgeSpaceService knowledgeSpaceService) {
        this.tenantService = tenantService;
        this.authService = authService;
        this.knowledgeSpaceService = knowledgeSpaceService;
    }

    /**
     * 校验当前用户是否有租户管理权限
     * 基于菜单权限编码（tenant:admin），可灵活分配无需硬编码 tenantId
     */
    private void checkTenantAdmin() {
        Long userId = LoginContextHolder.getUserId();
        if (userId == null) {
            throw new SecurityException("用户未登录");
        }
    }

    @Operation(summary = "Get Tenant List")
    @SaCheckPermission("system:tenant:list")
    @GetMapping("/list")
    public Result<List<TenantVO>> getAllTenants() {
        checkTenantAdmin();
        List<TenantBO> tenantBOs = tenantService.getAllTenants();
        List<TenantVO> tenantVOs = MapstructUtils.convert(tenantBOs, TenantVO.class);
        return Result.success(tenantVOs);
    }

    @Operation(summary = "Get Tenant Page")
    @SaCheckPermission("system:tenant:list")
    @GetMapping("/page")
    public Result<PageData<TenantVO>> getTenantPage(@Valid TenantPageRequest request) {
        checkTenantAdmin();
        return Result.success(tenantService.getTenantPage(
                request.getPageNum(), request.getPageSize(),
                request.getName(), request.getCode(), request.getStatus()));
    }

    @Operation(summary = "Get Tenant Detail")
    @SaCheckPermission("system:tenant:list")
    @GetMapping("/detail")
    public Result<TenantVO> getTenantById(@RequestParam Long id) {
        checkTenantAdmin();
        TenantBO tenantBO = tenantService.getTenantById(id);
        if (tenantBO == null) {
            return Result.fail("租户不存在");
        }
        TenantVO tenantVO = MapstructUtils.convert(tenantBO, TenantVO.class);
        return Result.success(tenantVO);
    }

    @Operation(summary = "Create Tenant")
    @SaCheckPermission("system:tenant:create")
    @PostMapping("/create")
    public Result<Void> createTenant(@Valid @RequestBody TenantRequest request) {
        checkTenantAdmin();
        TenantBO tenantBO = MapstructUtils.convert(request, TenantBO.class);
        boolean success = tenantService.createTenant(tenantBO);
        if (success) {
            knowledgeSpaceService.initializeTenantDefaults(tenantBO.getId());
            return Result.success();
        } else {
            return Result.fail("新增失败，租户编码可能已存在");
        }
    }

    @Operation(summary = "Update Tenant")
    @SaCheckPermission("system:tenant:update")
    @PostMapping("/update")
    public Result<Void> updateTenant(@RequestParam Long id, @Valid @RequestBody TenantRequest request) {
        checkTenantAdmin();
        TenantBO tenantBO = MapstructUtils.convert(request, TenantBO.class);
        boolean success = tenantService.updateTenant(id, tenantBO);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("修改失败，租户不存在或编码已存在");
        }
    }

    @Operation(summary = "Delete Tenant")
    @SaCheckPermission("system:tenant:delete")
    @PostMapping("/delete")
    public Result<Void> deleteTenant(@RequestParam Long id) {
        checkTenantAdmin();
        boolean success = tenantService.deleteTenant(id);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("删除失败，默认租户不可删除或租户不存在");
        }
    }
}
