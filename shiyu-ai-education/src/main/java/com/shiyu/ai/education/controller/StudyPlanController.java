package com.shiyu.ai.education.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.education.dto.DailyTaskResponse;
import com.shiyu.ai.education.dto.StudyPlanResponse;
import com.shiyu.ai.education.request.StudyPlanRequest;
import com.shiyu.ai.education.service.StudyPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/study-plan")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @GetMapping("/detail")
    public Result<StudyPlanResponse> getById(@RequestParam Long id) {
        return Result.success(studyPlanService.getById(id));
    }

    @GetMapping("/student")
    public Result<List<StudyPlanResponse>> listByStudentId(@RequestParam Long studentId) {
        return Result.success(studyPlanService.listByStudentId(studentId));
    }

    @GetMapping("/active")
    public Result<List<StudyPlanResponse>> listActiveByStudent(@RequestParam Long studentId) {
        return Result.success(studyPlanService.listActiveByStudent(studentId));
    }

    @GetMapping("/today-tasks")
    public Result<List<DailyTaskResponse>> getTodayTasks(@RequestParam Long studentId) {
        return Result.success(studyPlanService.getTodayTasks(studentId));
    }

    @PostMapping("/create")
    public Result<StudyPlanResponse> create(@Valid @RequestBody StudyPlanRequest request) {
        return Result.success(studyPlanService.create(request));
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody StudyPlanRequest request) {
        request.setId(id);
        studyPlanService.update(request);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        studyPlanService.deleteById(id);
        return Result.success();
    }
}
