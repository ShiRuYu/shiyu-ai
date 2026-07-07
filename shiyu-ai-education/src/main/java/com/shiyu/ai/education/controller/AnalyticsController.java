package com.shiyu.ai.education.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.dataobject.education.StudyRecordDO;
import com.shiyu.ai.education.service.AnalyticsService;
import com.shiyu.ai.education.dto.AbilityRadarResponse;
import com.shiyu.ai.education.dto.OverviewResponse;
import com.shiyu.ai.education.dto.TrendResponse;
import com.shiyu.ai.education.dto.WeakPointResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "学习分析")
@RestController
@RequestMapping("/edu/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/records")
    @Operation(summary = "获取学生学习记录")
    public Result<List<StudyRecordDO>> listRecordsByStudent(@RequestParam Long studentId) {
        List<StudyRecordDO> records = analyticsService.listRecordsByStudent(studentId);
        return Result.success(records);
    }

    @GetMapping("/records/knowledge")
    @Operation(summary = "获取学生知识点学习记录")
    public Result<List<StudyRecordDO>> listRecordsByStudentAndKnowledge(
            @RequestParam Long studentId, @RequestParam Long knowledgeId) {
        List<StudyRecordDO> records = analyticsService.listRecordsByStudentAndKnowledge(studentId, knowledgeId);
        return Result.success(records);
    }

    @PostMapping("/record-create")
    @Operation(summary = "创建学习记录")
    public Result<StudyRecordDO> createRecord(@Valid @RequestBody StudyRecordDO record) {
        StudyRecordDO created = analyticsService.createRecord(record);
        return Result.success(created);
    }

    @GetMapping("/ability-radar")
    @Operation(summary = "能力雷达图 - 获取 Bloom 六维度掌握度")
    public Result<AbilityRadarResponse> getAbilityRadar(
            @RequestParam Long studentId, @RequestParam Long knowledgeId) {
        return Result.success(analyticsService.getAbilityRadar(studentId, knowledgeId));
    }

    @GetMapping("/overview")
    @Operation(summary = "学习概览 - 总学习量、掌握度、连续学习天数")
    public Result<OverviewResponse> getOverview(@RequestParam Long studentId) {
        return Result.success(analyticsService.getOverview(studentId));
    }

    @GetMapping("/weak-points")
    @Operation(summary = "薄弱知识点 - 掌握度 < 60 的知识点列表")
    public Result<List<WeakPointResponse>> getWeakPoints(@RequestParam Long studentId) {
        return Result.success(analyticsService.getWeakPoints(studentId));
    }

    @GetMapping("/trend")
    @Operation(summary = "学习趋势 - 近7天每日学习记录数")
    public Result<TrendResponse> getTrend(@RequestParam Long studentId) {
        return Result.success(analyticsService.getTrend(studentId));
    }
}
