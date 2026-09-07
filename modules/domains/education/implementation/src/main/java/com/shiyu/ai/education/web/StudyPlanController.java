package com.shiyu.ai.education.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.education.dto.DailyTaskResponse;
import com.shiyu.ai.education.dto.StudyPlanResponse;
import com.shiyu.ai.education.request.StudyPlanRequest;
import com.shiyu.ai.education.service.StudyPlanService;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/study-plan")
@RequiredArgsConstructor
@SaCheckPermission("edu:plan:list")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @GetMapping("/detail")
    public Result<StudyPlanResponse> getById(@RequestParam Long id) {
        return Result.success(studyPlanService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    @GetMapping("/student")
    public Result<List<StudyPlanResponse>> listByStudentId(@RequestParam Long studentId) {
        return Result.success(studyPlanService.listByStudentId(ActorContextHttpAdapter.currentActor(), studentId));
    }

    @GetMapping("/active")
    public Result<List<StudyPlanResponse>> listActiveByStudent(@RequestParam Long studentId) {
        return Result.success(studyPlanService.listActiveByStudent(ActorContextHttpAdapter.currentActor(), studentId));
    }

    @GetMapping("/today-tasks")
    public Result<List<DailyTaskResponse>> getTodayTasks(@RequestParam Long studentId) {
        return Result.success(studyPlanService.getTodayTasks(ActorContextHttpAdapter.currentActor(), studentId));
    }

    @PostMapping("/create")
    @SaCheckPermission("edu:plan:list")
    public Result<StudyPlanResponse> create(@Valid @RequestBody StudyPlanRequest request) {
        return Result.success(studyPlanService.create(ActorContextHttpAdapter.currentActor(), request));
    }

    @PostMapping("/update")
    @SaCheckPermission("edu:plan:list")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody StudyPlanRequest request) {
        request.setId(id);
        studyPlanService.update(ActorContextHttpAdapter.currentActor(), request);
        return Result.success();
    }

    @PostMapping("/delete")
    @SaCheckPermission("edu:plan:list")
    public Result<Void> delete(@RequestParam Long id) {
        studyPlanService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
