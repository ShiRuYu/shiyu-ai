package com.shiyu.ai.education.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.dataobject.education.SubjectDO;
import com.shiyu.ai.education.dto.SubjectResponse;
import com.shiyu.ai.education.subject.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "学科管理")
@RestController
@RequestMapping("/api/subject")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping("/{id}")
    @Operation(summary = "获取学科详情")
    public Result<SubjectResponse> getById(@PathVariable Long id) {
        SubjectDO subject = subjectService.getById(id);
        return Result.success(toResponse(subject));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "根据编码获取学科")
    public Result<SubjectResponse> getByCode(@PathVariable String code) {
        SubjectDO subject = subjectService.getByCode(code);
        return Result.success(toResponse(subject));
    }

    @GetMapping
    @Operation(summary = "获取所有学科")
    public Result<List<SubjectResponse>> listAll() {
        List<SubjectDO> subjects = subjectService.listAll();
        return Result.success(subjects.stream().map(this::toResponse).toList());
    }

    @GetMapping("/grade-level/{gradeLevel}")
    @Operation(summary = "根据学段获取学科")
    public Result<List<SubjectResponse>> listByGradeLevel(@PathVariable String gradeLevel) {
        List<SubjectDO> subjects = subjectService.listByGradeLevel(gradeLevel);
        return Result.success(subjects.stream().map(this::toResponse).toList());
    }

    @PostMapping
    @Operation(summary = "创建学科")
    public Result<SubjectResponse> create(@Valid @RequestBody SubjectDO subject) {
        SubjectDO created = subjectService.create(subject);
        return Result.success(toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新学科")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SubjectDO subject) {
        subject.setId(id);
        subjectService.update(subject);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除学科")
    public Result<Void> delete(@PathVariable Long id) {
        subjectService.deleteById(id);
        return Result.success();
    }

    private SubjectResponse toResponse(SubjectDO subject) {
        if (subject == null) return null;
        return new SubjectResponse(
                subject.getId(), subject.getCode(), subject.getName(),
                subject.getGradeLevel(), subject.getIcon(), subject.getSortOrder());
    }
}
