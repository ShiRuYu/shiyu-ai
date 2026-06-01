package com.shiyu.ai.auth.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.auth.domain.bo.SysTenantBO;
import com.shiyu.ai.auth.service.SysTenantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 租户管理控制�? *
 * @author shiyu-ai
 */
@Tag(name = "�⻧����", description = "�⻧����ӿ�")
@RestController
@RequiredArgsConstructor
@RequestMapping("/tenants")
public class SysTenantController {

    private final SysTenantService sysTenantService;

    /**
     * 获取租户列表
     */
    @GetMapping
    public Result<Pair<Long, List<SysTenantBO>>> getTenants(@RequestParam(defaultValue = "1") Number pageNumber,
                                                             @RequestParam(defaultValue = "10") Number pageSize) {
        return Result.success(sysTenantService.getAll(pageNumber, pageSize));
    }

    /**
     * 获取租户详情
     */
    @GetMapping("/{id}")
    public Result<SysTenantBO> getTenant(@PathVariable Long id) {
        return Result.success(sysTenantService.getById(id));
    }

    /**
     * 创建租户
     */
    @PostMapping
    public Result<SysTenantBO> createTenant(@Valid @RequestBody SysTenantBO sysTenantBO) {
        return Result.success(sysTenantService.create(sysTenantBO));
    }

    /**
     * 更新租户
     */
    @PutMapping("/{id}")
    public Result<SysTenantBO> updateTenant(@PathVariable Long id, @Valid @RequestBody SysTenantBO sysTenantBO) {
        sysTenantBO.setId(id);
        return Result.success(sysTenantService.update(sysTenantBO));
    }

    /**
     * 删除租户
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTenant(@PathVariable Long id) {
        sysTenantService.deleteById(id);
        return Result.success();
    }
}


