package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.StudyPlanService;
import com.shiyu.ai.education.implementation.web.dto.DailyTaskResponse;
import com.shiyu.ai.education.implementation.web.dto.StudyPlanResponse;
import com.shiyu.ai.education.implementation.web.request.StudyPlanRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code StudyPlanController} 是教育模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/study-plan")
@RequiredArgsConstructor
@SaCheckPermission("edu:plan:list")
public class StudyPlanController {

    /**
     * studyPlanService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final StudyPlanService studyPlanService;

    /**
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/detail")
    public Result<StudyPlanResponse> getById(@RequestParam Long id) {
        return Result.success(studyPlanService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * {@code listByStudentId} 查询并返回当前操作所需的数据。
     *
     * @param studentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/student")
    public Result<List<StudyPlanResponse>> listByStudentId(@RequestParam Long studentId) {
        return Result.success(
                studyPlanService.listByStudentId(
                        ActorContextHttpAdapter.currentActor(), studentId));
    }

    /**
     * {@code listActiveByStudent} 查询并返回当前操作所需的数据。
     *
     * @param studentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/active")
    public Result<List<StudyPlanResponse>> listActiveByStudent(@RequestParam Long studentId) {
        return Result.success(
                studyPlanService.listActiveByStudent(
                        ActorContextHttpAdapter.currentActor(), studentId));
    }

    /**
     * {@code getTodayTasks} 查询并返回当前操作所需的数据。
     *
     * @param studentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/today-tasks")
    public Result<List<DailyTaskResponse>> getTodayTasks(@RequestParam Long studentId) {
        return Result.success(
                studyPlanService.getTodayTasks(ActorContextHttpAdapter.currentActor(), studentId));
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/create")
    @SaCheckPermission("edu:plan:list")
    public Result<StudyPlanResponse> create(@Valid @RequestBody StudyPlanRequest request) {
        return Result.success(
                studyPlanService.create(ActorContextHttpAdapter.currentActor(), request));
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
    @SaCheckPermission("edu:plan:list")
    public Result<Void> update(
            @RequestParam Long id, @Valid @RequestBody StudyPlanRequest request) {
        request.setId(id);
        studyPlanService.update(ActorContextHttpAdapter.currentActor(), request);
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
    @SaCheckPermission("edu:plan:list")
    public Result<Void> delete(@RequestParam Long id) {
        studyPlanService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
