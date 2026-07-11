package com.shiyu.ai.education.controller;

import com.shiyu.ai.common.core.api.PageData;
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
@RequestMapping("/edu/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/detail")
    @Operation(summary = "获取课程详情")
    public Result<CourseResponse> getById(@RequestParam Long id) {
        CourseDO course = courseService.getById(id);
        return Result.success(toResponse(course));
    }

    @GetMapping("/list")
    @Operation(summary = "分页获取课程")
    public Result<PageData<CourseResponse>> listAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageData<CourseDO> page = courseService.page(pageNum, pageSize);
        List<CourseResponse> items = page.getItems().stream().map(this::toResponse).toList();
        return Result.success(new PageData<>(items, page.getTotal()));
    }

    @GetMapping("/subject")
    @Operation(summary = "根据学科获取课程")
    public Result<List<CourseResponse>> listBySubjectCode(@RequestParam String subjectCode) {
        List<CourseDO> courses = courseService.listBySubjectCode(subjectCode);
        return Result.success(courses.stream().map(this::toResponse).toList());
    }

    @GetMapping("/grade")
    @Operation(summary = "根据年级获取课程")
    public Result<List<CourseResponse>> listByGrade(@RequestParam Integer grade) {
        List<CourseDO> courses = courseService.listByGrade(grade);
        return Result.success(courses.stream().map(this::toResponse).toList());
    }

    @PostMapping("/create")
    @Operation(summary = "创建课程")
    public Result<CourseResponse> create(@Valid @RequestBody CourseDO course) {
        CourseDO created = courseService.create(course);
        return Result.success(toResponse(created));
    }

    @PostMapping("/update")
    @Operation(summary = "更新课程")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody CourseDO course) {
        course.setId(id);
        courseService.update(course);
        return Result.success();
    }


    @PostMapping("/learn")
    @Operation(summary = "开始学习 - 记录学生学习课程")
    public Result<CourseResponse> startLearning(@RequestParam Long courseId, @RequestParam Long studentId) {
        log.info("开始学习: courseId={}, studentId={}", courseId, studentId);
        CourseDO course = courseService.getById(courseId);
        if (course == null) return Result.fail("课程不存在");
        return Result.success(toResponse(course));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除课程")
    public Result<Void> delete(@RequestParam Long id) {
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
    @GetMapping("/progress")
    @Operation(summary = "Get course learning progress")
    public Result<CourseProgressResponse> getProgress(
            @RequestParam Long courseId, @RequestParam Long studentId) {
        return Result.success(courseService.getProgress(courseId, studentId));
    }

    @PostMapping("/record-study")
    @Operation(summary = "Record study session")
    public Result<Void> recordStudy(
            @RequestParam Long studentId,
            @RequestParam Long courseId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) Long knowledgeId) {
        com.shiyu.ai.dal.dataobject.education.StudyRecordDO record =
                new com.shiyu.ai.dal.dataobject.education.StudyRecordDO();
        record.setStudentId(studentId);
        record.setKnowledgeId(knowledgeId);
        record.setRecordType("LEARN");
        record.setCreateTime(java.time.LocalDateTime.now());
        courseService.recordStudy(record);
        return Result.success();
    }
}
