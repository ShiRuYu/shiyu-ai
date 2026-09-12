package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
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
 * {@code SubjectController} 是教育模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/subject")
@RequiredArgsConstructor
@SaCheckPermission("edu:subject:list")
public class SubjectController {

    /**
     * 学科服务，表示当前对象中的对应属性。
     */
    private final SubjectService subjectService;

    /**
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/detail")
    public Result<SubjectResponse> getById(@RequestParam Long id) {
        return Result.success(subjectService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * {@code getByCode} 查询并返回当前操作所需的数据。
     *
     * @param code 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/code")
    public Result<SubjectResponse> getByCode(@RequestParam String code) {
        return Result.success(
                subjectService.getByCode(ActorContextHttpAdapter.currentActor(), code));
    }

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @param pageNum 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/list")
    public Result<PageData<SubjectResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(
                subjectService.page(ActorContextHttpAdapter.currentActor(), pageNum, pageSize));
    }

    /**
     * {@code listByGradeLevel} 查询并返回当前操作所需的数据。
     *
     * @param gradeLevel 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/grade-level")
    public Result<List<SubjectResponse>> listByGradeLevel(@RequestParam String gradeLevel) {
        return Result.success(
                subjectService.listByGradeLevel(
                        ActorContextHttpAdapter.currentActor(), gradeLevel));
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/create")
    @SaCheckPermission("edu:subject:create")
    public Result<SubjectResponse> create(@Valid @RequestBody SubjectRequest request) {
        return Result.success(
                subjectService.create(ActorContextHttpAdapter.currentActor(), request));
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/update")
    @SaCheckPermission("edu:subject:edit")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody SubjectRequest request) {
        request.setId(id);
        subjectService.update(ActorContextHttpAdapter.currentActor(), request);
        return Result.success();
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/delete")
    @SaCheckPermission("edu:subject:delete")
    public Result<Void> delete(@RequestParam Long id) {
        subjectService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
