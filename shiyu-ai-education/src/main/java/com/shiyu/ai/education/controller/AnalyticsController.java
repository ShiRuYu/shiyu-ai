package com.shiyu.ai.education.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.dataobject.education.StudyRecordDO;
import com.shiyu.ai.education.analytics.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "学习分析")
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/records/student/{studentId}")
    @Operation(summary = "获取学生学习记录")
    public Result<List<StudyRecordDO>> listRecordsByStudent(@PathVariable Long studentId) {
        List<StudyRecordDO> records = analyticsService.listRecordsByStudent(studentId);
        return Result.success(records);
    }

    @GetMapping("/records/student/{studentId}/knowledge/{knowledgeId}")
    @Operation(summary = "获取学生知识点学习记录")
    public Result<List<StudyRecordDO>> listRecordsByStudentAndKnowledge(
            @PathVariable Long studentId, @PathVariable Long knowledgeId) {
        List<StudyRecordDO> records = analyticsService.listRecordsByStudentAndKnowledge(studentId, knowledgeId);
        return Result.success(records);
    }

    @PostMapping("/records")
    @Operation(summary = "创建学习记录")
    public Result<StudyRecordDO> createRecord(@RequestBody StudyRecordDO record) {
        StudyRecordDO created = analyticsService.createRecord(record);
        return Result.success(created);
    }
}
