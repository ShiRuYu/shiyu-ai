package com.shiyu.ai.auth.controller;

import com.shiyu.ai.auth.service.TenantService;
import com.shiyu.ai.dal.auth.bo.TenantBO;
import com.shiyu.ai.auth.request.TenantRequest;
import com.shiyu.ai.auth.vo.TenantVO;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.MapstructUtils;
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

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    private void checkTenantAdmin() {
        Long tenantId = LoginContextHolder.getTenantId();
        if (tenantId == null || tenantId != 1L) {
            throw new SecurityException("仅默认租户可管理租户");
        }
    }

    @Operation(summary = "Get Tenant List")
    @GetMapping("/list")
    public Result<List<TenantVO>> getAllTenants() {
        checkTenantAdmin();
        List<TenantBO> tenantBOs = tenantService.getAllTenants();
        List<TenantVO> tenantVOs = MapstructUtils.convert(tenantBOs, TenantVO.class);
        return Result.success(tenantVOs);
    }

    @Operation(summary = "Get Tenant Detail")
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
    @PostMapping("/create")
    public Result<Void> createTenant(@Valid @RequestBody TenantRequest request) {
        checkTenantAdmin();
        TenantBO tenantBO = MapstructUtils.convert(request, TenantBO.class);
        boolean success = tenantService.createTenant(tenantBO);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("新增失败，租户编码可能已存在");
        }
    }

    @Operation(summary = "Update Tenant")
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
