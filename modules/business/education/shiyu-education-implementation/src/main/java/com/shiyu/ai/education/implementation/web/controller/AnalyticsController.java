package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.AnalyticsService;
import com.shiyu.ai.education.implementation.web.dto.*;
import com.shiyu.ai.education.implementation.web.dto.StudyRecordResponse;
import com.shiyu.ai.education.implementation.web.request.StudyRecordRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code AnalyticsController} 是教育模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@SaCheckPermission("edu:analytics")
public class AnalyticsController {

    /**
     * analyticsService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AnalyticsService analyticsService;

    /**
     * {@code listRecordsByStudent} 查询并返回当前操作所需的数据。
     *
     * @param studentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/records")
    public Result<List<StudyRecordResponse>> listRecordsByStudent(@RequestParam Long studentId) {
        return Result.success(
                analyticsService.listRecordsByStudent(
                        ActorContextHttpAdapter.currentActor(), studentId));
    }

    /**
     * {@code listRecordsByStudentAndKnowledge} 查询并返回当前操作所需的数据。
     *
     * @param studentId 参数值，用于执行当前操作。
     * @param knowledgeId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/records/knowledge")
    public Result<List<StudyRecordResponse>> listRecordsByStudentAndKnowledge(
            @RequestParam Long studentId, @RequestParam Long knowledgeId) {
        return Result.success(
                analyticsService.listRecordsByStudentAndKnowledge(
                        ActorContextHttpAdapter.currentActor(), studentId, knowledgeId));
    }

    /**
     * {@code createRecord} 写入或更新当前模块中的业务数据。
     *
     * @param record 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/record-create")
    public Result<StudyRecordResponse> createRecord(@Valid @RequestBody StudyRecordRequest record) {
        return Result.success(
                analyticsService.createRecord(ActorContextHttpAdapter.currentActor(), record));
    }

    /**
     * {@code getAbilityRadar} 查询并返回当前操作所需的数据。
     *
     * @param studentId 参数值，用于执行当前操作。
     * @param knowledgeId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/ability-radar")
    public Result<AbilityRadarResponse> getAbilityRadar(
            @RequestParam Long studentId, @RequestParam Long knowledgeId) {
        return Result.success(
                analyticsService.getAbilityRadar(
                        ActorContextHttpAdapter.currentActor(), studentId, knowledgeId));
    }

    /**
     * {@code getOverview} 查询并返回当前操作所需的数据。
     *
     * @param studentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/overview")
    public Result<OverviewResponse> getOverview(@RequestParam Long studentId) {
        return Result.success(
                analyticsService.getOverview(ActorContextHttpAdapter.currentActor(), studentId));
    }

    /**
     * {@code getWeakPoints} 查询并返回当前操作所需的数据。
     *
     * @param studentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/weak-points")
    public Result<List<WeakPointResponse>> getWeakPoints(@RequestParam Long studentId) {
        return Result.success(
                analyticsService.getWeakPoints(ActorContextHttpAdapter.currentActor(), studentId));
    }

    /**
     * {@code getTrend} 查询并返回当前操作所需的数据。
     *
     * @param studentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/trend")
    public Result<TrendResponse> getTrend(@RequestParam Long studentId) {
        return Result.success(
                analyticsService.getTrend(ActorContextHttpAdapter.currentActor(), studentId));
    }
}
