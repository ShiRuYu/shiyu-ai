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
 * 处理 分析 相关的 Web 请求，并将请求转换为应用服务调用。
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
     * 查询 分析 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param records 用于完成本次业务处理的 records 参数。
     */
    @GetMapping("/records")
    public Result<List<StudyRecordResponse>> listRecordsByStudent(@RequestParam Long studentId) {
        return Result.success(
                analyticsService.listRecordsByStudent(
                        ActorContextHttpAdapter.currentActor(), studentId));
    }

    /**
     * 查询 分析 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param knowledge 用于完成本次业务处理的 knowledge 参数。
     */
    @GetMapping("/records/knowledge")
    public Result<List<StudyRecordResponse>> listRecordsByStudentAndKnowledge(
            @RequestParam Long studentId, @RequestParam Long knowledgeId) {
        return Result.success(
                analyticsService.listRecordsByStudentAndKnowledge(
                        ActorContextHttpAdapter.currentActor(), studentId, knowledgeId));
    }

    /**
     * 执行 分析 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param create 用于完成本次业务处理的 create 参数。
     */
    @PostMapping("/record-create")
    public Result<StudyRecordResponse> createRecord(@Valid @RequestBody StudyRecordRequest record) {
        return Result.success(
                analyticsService.createRecord(ActorContextHttpAdapter.currentActor(), record));
    }

    /**
     * 查询 分析 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param radar 用于完成本次业务处理的 radar 参数。
     */
    @GetMapping("/ability-radar")
    public Result<AbilityRadarResponse> getAbilityRadar(
            @RequestParam Long studentId, @RequestParam Long knowledgeId) {
        return Result.success(
                analyticsService.getAbilityRadar(
                        ActorContextHttpAdapter.currentActor(), studentId, knowledgeId));
    }

    /**
     * 查询 分析 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param overview 用于完成本次业务处理的 overview 参数。
     */
    @GetMapping("/overview")
    public Result<OverviewResponse> getOverview(@RequestParam Long studentId) {
        return Result.success(
                analyticsService.getOverview(ActorContextHttpAdapter.currentActor(), studentId));
    }

    /**
     * 查询 分析 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param points 用于完成本次业务处理的 points 参数。
     */
    @GetMapping("/weak-points")
    public Result<List<WeakPointResponse>> getWeakPoints(@RequestParam Long studentId) {
        return Result.success(
                analyticsService.getWeakPoints(ActorContextHttpAdapter.currentActor(), studentId));
    }

    /**
     * 查询 分析 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param trend 用于完成本次业务处理的 trend 参数。
     */
    @GetMapping("/trend")
    public Result<TrendResponse> getTrend(@RequestParam Long studentId) {
        return Result.success(
                analyticsService.getTrend(ActorContextHttpAdapter.currentActor(), studentId));
    }
}
