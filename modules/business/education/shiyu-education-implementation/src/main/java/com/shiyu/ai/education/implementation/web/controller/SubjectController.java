package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.SubjectService;
import com.shiyu.ai.education.implementation.web.dto.SubjectResponse;
import com.shiyu.ai.education.implementation.web.request.SubjectRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 处理 学科 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/subject")
@RequiredArgsConstructor
public class SubjectController {

    /**
     * 学科服务，表示当前对象中的对应属性。
     */
    private final SubjectService subjectService;

    /**
     * 查询 学科 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param detail 用于完成本次业务处理的 detail 参数。
     */
    @SaCheckPermission("edu:subject:list")
    @GetMapping("/{id}")
    public Result<SubjectResponse> getById(@PathVariable Long id) {
        return Result.success(subjectService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * 查询 学科 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param code 用于定位或筛选目标业务对象的业务值。
     */
    @SaCheckPermission("edu:subject:list")
    @GetMapping("/code")
    public Result<SubjectResponse> getByCode(@RequestParam String code) {
        return Result.success(
                subjectService.getByCode(ActorContextHttpAdapter.currentActor(), code));
    }

    /**
     * 查询 学科 相关业务数据，并返回处理结果。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     * @return 返回 学科 相关操作生成的结果数据。
     */
    @SaCheckPermission("edu:subject:list")
    @GetMapping
    public Result<PageData<SubjectResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(
                subjectService.page(ActorContextHttpAdapter.currentActor(), pageNum, pageSize));
    }

    /**
     * 查询 学科 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param level 用于完成本次业务处理的 level 参数。
     */
    @SaCheckPermission("edu:subject:list")
    @GetMapping("/grade-level")
    public Result<List<SubjectResponse>> listByGradeLevel(@RequestParam String gradeLevel) {
        return Result.success(
                subjectService.listByGradeLevel(
                        ActorContextHttpAdapter.currentActor(), gradeLevel));
    }

    /**
     * 执行 学科 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param create 用于完成本次业务处理的 create 参数。
     */
    @PostMapping
    @SaCheckPermission("edu:subject:create")
    public Result<SubjectResponse> create(@Valid @RequestBody SubjectRequest request) {
        return Result.success(
                subjectService.create(ActorContextHttpAdapter.currentActor(), request));
    }

    /**
     * 执行 学科 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param update 用于完成本次业务处理的 update 参数。
     */
    @PutMapping("/{id}")
    @SaCheckPermission("edu:subject:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SubjectRequest request) {
        request.setId(id);
        subjectService.update(ActorContextHttpAdapter.currentActor(), request);
        return Result.success();
    }

    /**
     * 执行 学科 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param delete 用于完成本次业务处理的 delete 参数。
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("edu:subject:delete")
    public Result<Void> delete(@PathVariable Long id) {
        subjectService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
