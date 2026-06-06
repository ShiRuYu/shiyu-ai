package com.shiyu.ai.agent.biz.auth.controller;

import com.shiyu.ai.agent.biz.auth.service.DeptService;
import com.shiyu.ai.agent.domain.bo.DeptBO;
import com.shiyu.ai.agent.domain.request.DeptRequest;
import com.shiyu.ai.agent.domain.vo.DeptVO;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/dept")
public class DeptController {

    private final DeptService deptService;

    public DeptController(DeptService deptService) {
        this.deptService = deptService;
    }

    /**
     * 获取部门列表（树形）
     */
    @GetMapping("/list")
    public Result<List<DeptVO>> getDeptList(
            @RequestParam(required = false) String name) {
        log.info("获取部门列表，name: {}", name);

        List<DeptBO> deptBOs = deptService.getDeptList(name);
        List<DeptVO> deptVOs = MapstructUtils.convert(deptBOs, DeptVO.class);

        return Result.success(deptVOs);
    }

    /**
     * 新增部门
     */
    @PostMapping("")
    public Result<Void> createDept(@RequestBody DeptRequest request) {
        log.info("新增部门，name: {}", request.getName());

        DeptBO deptBO = MapstructUtils.convert(request, DeptBO.class);
        boolean success = deptService.createDept(deptBO);

        if (success) {
            return Result.success();
        } else {
            return Result.fail("新增失败");
        }
    }

    /**
     * 修改部门
     */
    @PatchMapping("/{id}")
    public Result<Void> updateDept(
            @PathVariable Long id,
            @RequestBody DeptRequest request) {
        log.info("修改部门，id: {}", id);

        DeptBO deptBO = MapstructUtils.convert(request, DeptBO.class);
        boolean success = deptService.updateDept(id, deptBO);

        if (success) {
            return Result.success();
        } else {
            return Result.fail("修改失败");
        }
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteDept(@PathVariable Long id) {
        log.info("删除部门，id: {}", id);

        boolean success = deptService.deleteDept(id);

        if (success) {
            return Result.success();
        } else {
            return Result.fail("删除失败，可能存在子部门");
        }
    }
}
