package com.shiyu.ai.auth.controller;

import com.shiyu.ai.auth.service.TenantService;
import com.shiyu.ai.model.bo.TenantBO;
import com.shiyu.ai.model.request.TenantRequest;
import com.shiyu.ai.model.vo.TenantVO;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
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
            throw new SecurityException("浠呴粯璁ょ鎴峰彲绠＄悊绉熸埛");
        }
    }

    @GetMapping("/all")
    public Result<List<TenantVO>> getAllTenants() {
        checkTenantAdmin();
        List<TenantBO> tenantBOs = tenantService.getAllTenants();
        List<TenantVO> tenantVOs = MapstructUtils.convert(tenantBOs, TenantVO.class);
        return Result.success(tenantVOs);
    }

    @GetMapping("/{id}")
    public Result<TenantVO> getTenantById(@PathVariable Long id) {
        checkTenantAdmin();
        TenantBO tenantBO = tenantService.getTenantById(id);
        if (tenantBO == null) {
            return Result.fail("绉熸埛涓嶅瓨鍦?);
        }
        TenantVO tenantVO = MapstructUtils.convert(tenantBO, TenantVO.class);
        return Result.success(tenantVO);
    }

    @PostMapping("")
    public Result<Void> createTenant(@Valid @RequestBody TenantRequest request) {
        checkTenantAdmin();
        TenantBO tenantBO = MapstructUtils.convert(request, TenantBO.class);
        boolean success = tenantService.createTenant(tenantBO);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("鏂板澶辫触锛岀鎴风紪鐮佸彲鑳藉凡瀛樺湪");
        }
    }

    @PatchMapping("/{id}")
    public Result<Void> updateTenant(@PathVariable Long id, @Valid @RequestBody TenantRequest request) {
        checkTenantAdmin();
        TenantBO tenantBO = MapstructUtils.convert(request, TenantBO.class);
        boolean success = tenantService.updateTenant(id, tenantBO);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("淇敼澶辫触锛岀鎴蜂笉瀛樺湪鎴栫紪鐮佸凡瀛樺湪");
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTenant(@PathVariable Long id) {
        checkTenantAdmin();
        boolean success = tenantService.deleteTenant(id);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("鍒犻櫎澶辫触锛岄粯璁ょ鎴蜂笉鍙垹闄ゆ垨绉熸埛涓嶅瓨鍦?);
        }
    }
}
