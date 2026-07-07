package com.shiyu.ai.education.controller;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.dataobject.education.QuestionDO;
import com.shiyu.ai.education.dto.QuestionResponse;
import com.shiyu.ai.education.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "题目管理")
@RestController
@RequestMapping("/api/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    @Operation(summary = "分页获取题目")
    public Result<PageData<QuestionResponse>> listAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageData<QuestionDO> page = questionService.page(pageNum, pageSize);
        List<QuestionResponse> items = page.getItems().stream().map(this::toResponse).toList();
        return Result.success(new PageData<>(items, page.getTotal()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取题目详情")
    public Result<QuestionResponse> getById(@PathVariable Long id) {
        QuestionDO question = questionService.getById(id);
        return Result.success(toResponse(question));
    }

    @GetMapping("/subject/{subjectCode}/grade/{grade}")
    @Operation(summary = "根据学科和年级获取题目")
    public Result<List<QuestionResponse>> listBySubjectAndGrade(
            @PathVariable String subjectCode, @PathVariable Integer grade) {
        List<QuestionDO> questions = questionService.listBySubjectAndGrade(subjectCode, grade);
        return Result.success(questions.stream().map(this::toResponse).toList());
    }

    @GetMapping("/difficulty/{difficulty}")
    @Operation(summary = "根据难度获取题目")
    public Result<List<QuestionResponse>> listByDifficulty(@PathVariable Integer difficulty) {
        List<QuestionDO> questions = questionService.listByDifficulty(difficulty);
        return Result.success(questions.stream().map(this::toResponse).toList());
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "根据类型获取题目")
    public Result<List<QuestionResponse>> listByType(@PathVariable String type) {
        List<QuestionDO> questions = questionService.listByType(type);
        return Result.success(questions.stream().map(this::toResponse).toList());
    }

    @PostMapping
    @Operation(summary = "创建题目")
    public Result<QuestionResponse> create(@Valid @RequestBody QuestionDO question) {
        QuestionDO created = questionService.create(question);
        return Result.success(toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新题目")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody QuestionDO question) {
        question.setId(id);
        questionService.update(question);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除题目")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.deleteById(id);
        return Result.success();
    }

    private QuestionResponse toResponse(QuestionDO question) {
        if (question == null) return null;
        return new QuestionResponse(
                question.getId(), question.getCode(), question.getType(),
                question.getSubjectCode(), question.getGrade(), question.getDifficulty(),
                question.getAbilityDimension(), question.getTitle(), question.getOptions(),
                question.getAnswer(), question.getAnalysis(), question.getTags(),
                question.getUsedCount());
    }
}
