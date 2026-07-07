package com.shiyu.ai.education.controller;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.dataobject.education.ExamDO;
import com.shiyu.ai.education.dto.ExamResponse;
import com.shiyu.ai.education.dto.SubmitAnswerRequest;
import com.shiyu.ai.education.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "考试管理")
@RestController
@RequestMapping("/edu/exam")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping("/list")
    @Operation(summary = "分页获取考试列表")
    public Result<PageData<ExamResponse>> listAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageData<ExamDO> page = examService.page(pageNum, pageSize);
        List<ExamResponse> items = page.getItems().stream().map(this::toResponse).toList();
        return Result.success(new PageData<>(items, page.getTotal()));
    }

    @GetMapping("/detail")
    @Operation(summary = "获取考试详情")
    public Result<ExamResponse> getById(@RequestParam Long id) {
        ExamDO exam = examService.getById(id);
        return Result.success(toResponse(exam));
    }

    @GetMapping("/subject")
    @Operation(summary = "根据学科获取考试")
    public Result<List<ExamResponse>> listBySubjectCode(@RequestParam String subjectCode) {
        List<ExamDO> exams = examService.listBySubjectCode(subjectCode);
        return Result.success(exams.stream().map(this::toResponse).toList());
    }

    @GetMapping("/teacher")
    @Operation(summary = "根据教师获取考试")
    public Result<List<ExamResponse>> listByTeacherId(@RequestParam Long teacherId) {
        List<ExamDO> exams = examService.listByTeacherId(teacherId);
        return Result.success(exams.stream().map(this::toResponse).toList());
    }

    @PostMapping("/create")
    @Operation(summary = "创建考试")
    public Result<ExamResponse> create(@Valid @RequestBody ExamDO exam) {
        ExamDO created = examService.create(exam);
        return Result.success(toResponse(created));
    }

    @PostMapping("/update")
    @Operation(summary = "更新考试")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody ExamDO exam) {
        exam.setId(id);
        examService.update(exam);
        return Result.success();
    }


    @PostMapping("/submit")
    @Operation(summary = "交卷 - 提交考试答案")
    public Result<ExamResponse> submit(@RequestParam Long id, @Valid @RequestBody SubmitAnswerRequest request) {
        ExamDO exam = examService.getById(id);
        if (exam == null) return Result.fail("考试不存在");
        log.info("交卷: examId={}, studentId={}", id, request.studentId());
        return Result.success(toResponse(exam));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除考试")
    public Result<Void> delete(@RequestParam Long id) {
        examService.deleteById(id);
        return Result.success();
    }

    private ExamResponse toResponse(ExamDO exam) {
        if (exam == null) return null;
        return new ExamResponse(
                exam.getId(), exam.getName(), exam.getType(),
                exam.getSubjectCode(), exam.getGrade(), exam.getDurationMin(),
                exam.getTotalScore(), exam.getStatus());
    }
}
