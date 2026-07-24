package com.shiyu.ai.web.education.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.education.dto.ExamResponse;
import com.shiyu.ai.education.request.ExamRequest;
import com.shiyu.ai.education.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/exam")
@RequiredArgsConstructor
@SaCheckPermission("edu:exam:list")
public class ExamController {

    private final ExamService examService;

    @GetMapping("/detail")
    public Result<ExamResponse> getById(@RequestParam Long id) {
        return Result.success(examService.getById(id));
    }

    @GetMapping("/list")
    public Result<PageData<ExamResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(examService.page(pageNum, pageSize));
    }

    @GetMapping("/subject")
    public Result<List<ExamResponse>> listBySubjectCode(@RequestParam String subjectCode) {
        return Result.success(examService.listBySubjectCode(subjectCode));
    }

    @GetMapping("/teacher")
    public Result<List<ExamResponse>> listByTeacherId(@RequestParam Long teacherId) {
        return Result.success(examService.listByTeacherId(teacherId));
    }

    @PostMapping("/create")
    @SaCheckPermission("edu:exam:create")
    public Result<ExamResponse> create(@Valid @RequestBody ExamRequest request) {
        return Result.success(examService.create(request));
    }

    @PostMapping("/update")
    @SaCheckPermission("edu:exam:edit")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody ExamRequest request) {
        request.setId(id);
        examService.update(request);
        return Result.success();
    }

    @PostMapping("/delete")
    @SaCheckPermission("edu:exam:delete")
    public Result<Void> delete(@RequestParam Long id) {
        examService.deleteById(id);
        return Result.success();
    }
}
