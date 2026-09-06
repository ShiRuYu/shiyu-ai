package com.shiyu.ai.education.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.education.request.StudyRecordRequest;
import com.shiyu.ai.education.dto.StudyRecordResponse;
import com.shiyu.ai.education.dto.*;
import com.shiyu.ai.education.service.AnalyticsService;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@SaCheckPermission("edu:analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/records")
    public Result<List<StudyRecordResponse>> listRecordsByStudent(@RequestParam Long studentId) {
        return Result.success(analyticsService.listRecordsByStudent(ActorContextHttpAdapter.currentActor(), studentId));
    }

    @GetMapping("/records/knowledge")
    public Result<List<StudyRecordResponse>> listRecordsByStudentAndKnowledge(
            @RequestParam Long studentId, @RequestParam Long knowledgeId) {
        return Result.success(analyticsService.listRecordsByStudentAndKnowledge(ActorContextHttpAdapter.currentActor(), studentId, knowledgeId));
    }

    @PostMapping("/record-create")
    public Result<StudyRecordResponse> createRecord(@Valid @RequestBody StudyRecordRequest record) {
        return Result.success(analyticsService.createRecord(ActorContextHttpAdapter.currentActor(), record));
    }

    @GetMapping("/ability-radar")
    public Result<AbilityRadarResponse> getAbilityRadar(
            @RequestParam Long studentId, @RequestParam Long knowledgeId) {
        return Result.success(analyticsService.getAbilityRadar(ActorContextHttpAdapter.currentActor(), studentId, knowledgeId));
    }

    @GetMapping("/overview")
    public Result<OverviewResponse> getOverview(@RequestParam Long studentId) {
        return Result.success(analyticsService.getOverview(ActorContextHttpAdapter.currentActor(), studentId));
    }

    @GetMapping("/weak-points")
    public Result<List<WeakPointResponse>> getWeakPoints(@RequestParam Long studentId) {
        return Result.success(analyticsService.getWeakPoints(ActorContextHttpAdapter.currentActor(), studentId));
    }

    @GetMapping("/trend")
    public Result<TrendResponse> getTrend(@RequestParam Long studentId) {
        return Result.success(analyticsService.getTrend(ActorContextHttpAdapter.currentActor(), studentId));
    }
}
