package com.shiyu.ai.education.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.education.dto.SubjectResponse;
import com.shiyu.ai.education.dto.SubjectResponse;
import com.shiyu.ai.education.request.SubjectRequest;
import com.shiyu.ai.education.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/subject")
@RequiredArgsConstructor
@SaCheckPermission("edu:subject:list")
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping("/detail")
    public Result<SubjectResponse> getById(@RequestParam Long id) {
        return Result.success(subjectService.getById(id));
    }

    @GetMapping("/code")
    public Result<SubjectResponse> getByCode(@RequestParam String code) {
        return Result.success(subjectService.getByCode(code));
    }

    @GetMapping("/list")
    public Result<PageData<SubjectResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(subjectService.page(pageNum, pageSize));
    }

    @GetMapping("/grade-level")
    public Result<List<SubjectResponse>> listByGradeLevel(@RequestParam String gradeLevel) {
        return Result.success(subjectService.listByGradeLevel(gradeLevel));
    }

    @PostMapping("/create")
    @SaCheckPermission("edu:subject:create")
    public Result<SubjectResponse> create(@Valid @RequestBody SubjectRequest request) {
        return Result.success(subjectService.create(request));
    }

    @PostMapping("/update")
    @SaCheckPermission("edu:subject:edit")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody SubjectRequest request) {
        request.setId(id);
        subjectService.update(request);
        return Result.success();
    }

    @PostMapping("/delete")
    @SaCheckPermission("edu:subject:delete")
    public Result<Void> delete(@RequestParam Long id) {
        subjectService.deleteById(id);
        return Result.success();
    }
}
