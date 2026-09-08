package com.shiyu.ai.education.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.education.dto.CourseResponse;
import com.shiyu.ai.education.request.CourseRequest;
import com.shiyu.ai.education.service.CourseService;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
@SaCheckPermission("edu:course:list")
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/detail")
    public Result<CourseResponse> getById(@RequestParam Long id) {
        return Result.success(courseService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    @GetMapping("/list")
    public Result<PageData<CourseResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(courseService.page(ActorContextHttpAdapter.currentActor(), pageNum, pageSize));
    }

    @GetMapping("/subject")
    public Result<List<CourseResponse>> listBySubjectCode(@RequestParam String subjectCode) {
        return Result.success(courseService.listBySubjectCode(ActorContextHttpAdapter.currentActor(), subjectCode));
    }

    @GetMapping("/grade")
    public Result<List<CourseResponse>> listByGrade(@RequestParam Integer grade) {
        return Result.success(courseService.listByGrade(ActorContextHttpAdapter.currentActor(), grade));
    }

    @PostMapping("/create")
    @SaCheckPermission("edu:course:create")
    public Result<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        return Result.success(courseService.create(ActorContextHttpAdapter.currentActor(), request));
    }

    @PostMapping("/update")
    @SaCheckPermission("edu:course:edit")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody CourseRequest request) {
        request.setId(id);
        courseService.update(ActorContextHttpAdapter.currentActor(), request);
        return Result.success();
    }

    @PostMapping("/learn")
    public Result<CourseResponse> startLearning(@RequestParam Long courseId, @RequestParam Long studentId) {
        CourseResponse course = courseService.getById(ActorContextHttpAdapter.currentActor(), courseId);
        return Result.success(course);
    }

    @PostMapping("/delete")
    @SaCheckPermission("edu:course:delete")
    public Result<Void> delete(@RequestParam Long id) {
        courseService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
