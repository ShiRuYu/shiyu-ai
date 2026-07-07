package com.shiyu.ai.education.controller;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.dataobject.education.SubjectDO;
import com.shiyu.ai.education.dto.SubjectResponse;
import com.shiyu.ai.education.service.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "学科管理")
@RestController
@RequestMapping("/edu/subject")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping("/detail")
    @Operation(summary = "获取学科详情")
    public Result<SubjectResponse> getById(@RequestParam Long id) {
        SubjectDO subject = subjectService.getById(id);
        return Result.success(toResponse(subject));
    }

    @GetMapping("/code")
    @Operation(summary = "根据编码获取学科")
    public Result<SubjectResponse> getByCode(@RequestParam String code) {
        SubjectDO subject = subjectService.getByCode(code);
        return Result.success(toResponse(subject));
    }

    @GetMapping("/list")
    @Operation(summary = "分页获取学科")
    public Result<PageData<SubjectResponse>> listAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageData<SubjectDO> page = subjectService.page(pageNum, pageSize);
        List<SubjectResponse> items = page.getItems().stream().map(this::toResponse).toList();
        return Result.success(new PageData<>(items, page.getTotal()));
    }

    @GetMapping("/grade-level")
    @Operation(summary = "根据学段获取学科")
    public Result<List<SubjectResponse>> listByGradeLevel(@RequestParam String gradeLevel) {
        List<SubjectDO> subjects = subjectService.listByGradeLevel(gradeLevel);
        return Result.success(subjects.stream().map(this::toResponse).toList());
    }

    @PostMapping("/create")
    @Operation(summary = "创建学科")
    public Result<SubjectResponse> create(@Valid @RequestBody SubjectDO subject) {
        SubjectDO created = subjectService.create(subject);
        return Result.success(toResponse(created));
    }

    @PostMapping("/update")
    @Operation(summary = "更新学科")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody SubjectDO subject) {
        subject.setId(id);
        subjectService.update(subject);
        return Result.success();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除学科")
    public Result<Void> delete(@RequestParam Long id) {
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
