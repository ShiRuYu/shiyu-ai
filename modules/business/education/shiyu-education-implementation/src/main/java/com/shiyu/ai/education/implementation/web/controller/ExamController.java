package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.ExamService;
import com.shiyu.ai.education.implementation.web.dto.ExamResponse;
import com.shiyu.ai.education.implementation.web.request.ExamRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code ExamController} 是教育模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/exam")
@RequiredArgsConstructor
@SaCheckPermission("edu:exam:list")
public class ExamController {

    /**
     * 考试服务，表示当前对象中的对应属性。
     */
    private final ExamService examService;

    /**
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/detail")
    public Result<ExamResponse> getById(@RequestParam Long id) {
        return Result.success(examService.getById(ActorContextHttpAdapter.currentActor(), id));
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
    public Result<PageData<ExamResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(
                examService.page(ActorContextHttpAdapter.currentActor(), pageNum, pageSize));
    }

    /**
     * {@code listBySubjectCode} 查询并返回当前操作所需的数据。
     *
     * @param subjectCode 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/subject")
    public Result<List<ExamResponse>> listBySubjectCode(@RequestParam String subjectCode) {
        return Result.success(
                examService.listBySubjectCode(ActorContextHttpAdapter.currentActor(), subjectCode));
    }

    /**
     * {@code listByTeacherId} 查询并返回当前操作所需的数据。
     *
     * @param teacherId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/teacher")
    public Result<List<ExamResponse>> listByTeacherId(@RequestParam Long teacherId) {
        return Result.success(
                examService.listByTeacherId(ActorContextHttpAdapter.currentActor(), teacherId));
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/create")
    @SaCheckPermission("edu:exam:create")
    public Result<ExamResponse> create(@Valid @RequestBody ExamRequest request) {
        return Result.success(examService.create(ActorContextHttpAdapter.currentActor(), request));
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
    @SaCheckPermission("edu:exam:edit")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody ExamRequest request) {
        request.setId(id);
        examService.update(ActorContextHttpAdapter.currentActor(), request);
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
    @SaCheckPermission("edu:exam:delete")
    public Result<Void> delete(@RequestParam Long id) {
        examService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
