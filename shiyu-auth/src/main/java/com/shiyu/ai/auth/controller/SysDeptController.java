package com.shiyu.ai.auth.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.auth.domain.bo.SysDeptBO;
import com.shiyu.ai.auth.service.SysDeptService;
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
 * 部门管理控制�? *
 * @author shiyu-ai
 */
@Tag(name = "���Ź���", description = "���Ź���ӿ�")
@RestController
@RequiredArgsConstructor
@RequestMapping("/depts")
public class SysDeptController {

    private final SysDeptService sysDeptService;

    /**
     * 获取部门列表
     */
    @GetMapping
    public Result<Pair<Long, List<SysDeptBO>>> getDepts(@RequestParam(defaultValue = "1") Number pageNumber,
                                                         @RequestParam(defaultValue = "10") Number pageSize) {
        return Result.success(sysDeptService.getAll(pageNumber, pageSize));
    }

    /**
     * 获取部门详情
     */
    @GetMapping("/{deptId}")
    public Result<SysDeptBO> getDept(@PathVariable Long deptId) {
        return Result.success(sysDeptService.getById(deptId));
    }

    /**
     * 创建部门
     */
    @PostMapping
    public Result<SysDeptBO> createDept(@Valid @RequestBody SysDeptBO sysDeptBO) {
        return Result.success(sysDeptService.create(sysDeptBO));
    }

    /**
     * 更新部门
     */
    @PutMapping("/{deptId}")
    public Result<SysDeptBO> updateDept(@PathVariable Long deptId, @Valid @RequestBody SysDeptBO sysDeptBO) {
        sysDeptBO.setDeptId(deptId);
        return Result.success(sysDeptService.update(sysDeptBO));
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{deptId}")
    public Result<Void> deleteDept(@PathVariable Long deptId) {
        sysDeptService.deleteById(deptId);
        return Result.success();
    }
}


