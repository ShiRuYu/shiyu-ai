package com.shiyu.ai.education.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.dataobject.education.CourseDO;
import com.shiyu.ai.education.dto.CourseProgressResponse;
import com.shiyu.ai.education.service.CourseService;
import com.shiyu.ai.education.dto.CourseResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "课程管理")
@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/{id}")
    @Operation(summary = "获取课程详情")
    public Result<CourseResponse> getById(@PathVariable Long id) {
        CourseDO course = courseService.getById(id);
        return Result.success(toResponse(course));
    }

    @GetMapping
    @Operation(summary = "获取所有课程")
    public Result<List<CourseResponse>> listAll() {
        List<CourseDO> courses = courseService.listAll();
        return Result.success(courses.stream().map(this::toResponse).toList());
    }

    @GetMapping("/subject/{subjectCode}")
    @Operation(summary = "根据学科获取课程")
    public Result<List<CourseResponse>> listBySubjectCode(@PathVariable String subjectCode) {
        List<CourseDO> courses = courseService.listBySubjectCode(subjectCode);
        return Result.success(courses.stream().map(this::toResponse).toList());
    }

    @GetMapping("/grade/{grade}")
    @Operation(summary = "根据年级获取课程")
    public Result<List<CourseResponse>> listByGrade(@PathVariable Integer grade) {
        List<CourseDO> courses = courseService.listByGrade(grade);
        return Result.success(courses.stream().map(this::toResponse).toList());
    }

    @PostMapping
    @Operation(summary = "创建课程")
    public Result<CourseResponse> create(@Valid @RequestBody CourseDO course) {
        CourseDO created = courseService.create(course);
        return Result.success(toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新课程")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CourseDO course) {
        course.setId(id);
        courseService.update(course);
        return Result.success();
    }


    @PostMapping("/{courseId}/learn")
    @Operation(summary = "开始学习 - 记录学生学习课程")
    public Result<CourseResponse> startLearning(@PathVariable Long courseId, @RequestParam Long studentId) {
        log.info("开始学习: courseId={}, studentId={}", courseId, studentId);
        CourseDO course = courseService.getById(courseId);
        if (course == null) return Result.fail("课程不存在");
        return Result.success(toResponse(course));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除课程")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.deleteById(id);
        return Result.success();
    }

    private CourseResponse toResponse(CourseDO course) {
        if (course == null) return null;
        return new CourseResponse(
                course.getId(), course.getName(), course.getDescription(),
                course.getSubjectCode(), course.getGrade(), course.getTextbookId(),
                course.getTeacherId(), course.getCoverUrl(), course.getTotalHours(),
                course.getStatus());
    }
}
