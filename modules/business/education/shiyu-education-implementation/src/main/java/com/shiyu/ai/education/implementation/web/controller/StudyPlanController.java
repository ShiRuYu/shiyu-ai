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
 * 处理 Study Plan 相关的 Web 请求，并将请求转换为应用服务调用。
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
     * 查询 Study Plan 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param detail 用于完成本次业务处理的 detail 参数。
     */
    @GetMapping("/detail")
    public Result<StudyPlanResponse> getById(@RequestParam Long id) {
        return Result.success(studyPlanService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * 查询 Study Plan 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param student 用于完成本次业务处理的 student 参数。
     */
    @GetMapping("/student")
    public Result<List<StudyPlanResponse>> listByStudentId(@RequestParam Long studentId) {
        return Result.success(
                studyPlanService.listByStudentId(
                        ActorContextHttpAdapter.currentActor(), studentId));
    }

    /**
     * 查询 Study Plan 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param active 用于完成本次业务处理的 active 参数。
     */
    @GetMapping("/active")
    public Result<List<StudyPlanResponse>> listActiveByStudent(@RequestParam Long studentId) {
        return Result.success(
                studyPlanService.listActiveByStudent(
                        ActorContextHttpAdapter.currentActor(), studentId));
    }

    /**
     * 查询 Study Plan 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tasks 用于完成本次业务处理的 tasks 参数。
     */
    @GetMapping("/today-tasks")
    public Result<List<DailyTaskResponse>> getTodayTasks(@RequestParam Long studentId) {
        return Result.success(
                studyPlanService.getTodayTasks(ActorContextHttpAdapter.currentActor(), studentId));
    }

    /**
     * 执行 Study Plan 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param create 用于完成本次业务处理的 create 参数。
     */
    @PostMapping("/create")
    @SaCheckPermission("edu:plan:list")
    public Result<StudyPlanResponse> create(@Valid @RequestBody StudyPlanRequest request) {
        return Result.success(
                studyPlanService.create(ActorContextHttpAdapter.currentActor(), request));
    }

    /**
     * 执行 Study Plan 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param update 用于完成本次业务处理的 update 参数。
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
     * 执行 Study Plan 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param delete 用于完成本次业务处理的 delete 参数。
     */
    @PostMapping("/delete")
    @SaCheckPermission("edu:plan:list")
    public Result<Void> delete(@RequestParam Long id) {
        studyPlanService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
