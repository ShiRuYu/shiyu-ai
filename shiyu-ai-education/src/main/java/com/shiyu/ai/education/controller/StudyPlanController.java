package com.shiyu.ai.education.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.dataobject.education.StudyPlanDO;
import com.shiyu.ai.education.dto.DailyTaskResponse;
import com.shiyu.ai.education.dto.StudyPlanResponse;
import com.shiyu.ai.education.plan.StudyPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "学习计划")
@RestController
@RequestMapping("/api/plan")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @GetMapping("/{id}")
    @Operation(summary = "获取学习计划详情")
    public Result<StudyPlanResponse> getById(@PathVariable Long id) {
        StudyPlanDO plan = studyPlanService.getById(id);
        return Result.success(toResponse(plan));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "获取学生学习计划")
    public Result<List<StudyPlanResponse>> listByStudentId(@PathVariable Long studentId) {
        List<StudyPlanDO> plans = studyPlanService.listByStudentId(studentId);
        return Result.success(plans.stream().map(this::toResponse).toList());
    }

    @GetMapping("/student/{studentId}/active")
    @Operation(summary = "获取学生活跃计划")
    public Result<List<StudyPlanResponse>> listActiveByStudent(@PathVariable Long studentId) {
        List<StudyPlanDO> plans = studyPlanService.listActiveByStudent(studentId);
        return Result.success(plans.stream().map(this::toResponse).toList());
    }

    @PostMapping
    @Operation(summary = "创建学习计划")
    public Result<StudyPlanResponse> create(@Valid @RequestBody StudyPlanDO plan) {
        StudyPlanDO created = studyPlanService.create(plan);
        return Result.success(toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新学习计划")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody StudyPlanDO plan) {
        plan.setId(id);
        studyPlanService.update(plan);
        return Result.success();
    }


    @GetMapping("/today/{studentId}")
    @Operation(summary = "今日任务 - 获取学生今日学习计划明细")
    public Result<List<DailyTaskResponse>> getTodayTasks(@PathVariable Long studentId) {
        List<StudyPlanDO> plans = studyPlanService.listActiveByStudent(studentId);
        if (plans.isEmpty()) {
            return Result.success(java.util.Collections.emptyList());
        }
        java.time.LocalDate today = java.time.LocalDate.now();
        java.util.List<DailyTaskResponse> tasks = new java.util.ArrayList<>();
        for (StudyPlanDO plan : plans) {
            if (plan.getStartDate() != null && plan.getEndDate() != null
                    && !today.isBefore(plan.getStartDate()) && !today.isAfter(plan.getEndDate())) {
                tasks.add(new DailyTaskResponse(
                        plan.getId(), plan.getTargetKnowledgeId(), plan.getName(),
                        today.toString(), plan.getStatus(), 0));
            }
        }
        return Result.success(tasks);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除学习计划")
    public Result<Void> delete(@PathVariable Long id) {
        studyPlanService.deleteById(id);
        return Result.success();
    }

    private StudyPlanResponse toResponse(StudyPlanDO plan) {
        if (plan == null) return null;
        return new StudyPlanResponse(
                plan.getId(), plan.getStudentId(), plan.getName(),
                plan.getStartDate() != null ? plan.getStartDate().toString() : null,
                plan.getEndDate() != null ? plan.getEndDate().toString() : null,
                plan.getStatus(), null, null, null);
    }
}
