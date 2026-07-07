package com.shiyu.ai.education.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.dataobject.education.WrongQuestionDO;
import com.shiyu.ai.education.dto.WrongQuestionResponse;
import com.shiyu.ai.education.service.WrongQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "错题本")
@RestController
@RequestMapping("/edu/wrong-question")
@RequiredArgsConstructor
public class WrongQuestionController {

    private final WrongQuestionService wrongQuestionService;

    @GetMapping("/detail")
    @Operation(summary = "获取错题详情")
    public Result<WrongQuestionResponse> getById(@RequestParam Long id) {
        WrongQuestionDO wrongQuestion = wrongQuestionService.getById(id);
        return Result.success(toResponse(wrongQuestion));
    }

    @GetMapping("/student")
    @Operation(summary = "获取学生错题列表")
    public Result<List<WrongQuestionResponse>> listByStudentId(@RequestParam Long studentId) {
        List<WrongQuestionDO> wrongQuestions = wrongQuestionService.listByStudentId(studentId);
        return Result.success(wrongQuestions.stream().map(this::toResponse).toList());
    }

    @PostMapping("/create")
    @Operation(summary = "添加错题")
    public Result<WrongQuestionResponse> create(@Valid @RequestBody WrongQuestionDO wrongQuestion) {
        WrongQuestionDO created = wrongQuestionService.create(wrongQuestion);
        return Result.success(toResponse(created));
    }

    @PostMapping("/update")
    @Operation(summary = "更新错题")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody WrongQuestionDO wrongQuestion) {
        wrongQuestion.setId(id);
        wrongQuestionService.update(wrongQuestion);
        return Result.success();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除错题")
    public Result<Void> delete(@RequestParam Long id) {
        wrongQuestionService.deleteById(id);
        return Result.success();
    }

    private WrongQuestionResponse toResponse(WrongQuestionDO wrongQuestion) {
        if (wrongQuestion == null) return null;
        return new WrongQuestionResponse(
                wrongQuestion.getId(), wrongQuestion.getStudentId(),
                wrongQuestion.getQuestionId(), wrongQuestion.getKnowledgeId(),
                null, wrongQuestion.getStudentAnswer(), null,
                wrongQuestion.getCorrectTimes());
    }
}
