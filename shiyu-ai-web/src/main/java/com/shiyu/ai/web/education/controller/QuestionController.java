package com.shiyu.ai.web.education.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.education.dto.QuestionResponse;
import com.shiyu.ai.education.dto.QuestionResponse;
import com.shiyu.ai.education.request.QuestionRequest;
import com.shiyu.ai.education.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/question")
@RequiredArgsConstructor
@SaCheckPermission("edu:question:list")
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/list")
    public Result<PageData<QuestionResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(questionService.page(pageNum, pageSize));
    }

    @GetMapping("/detail")
    public Result<QuestionResponse> getById(@RequestParam Long id) {
        return Result.success(questionService.getById(id));
    }

    @GetMapping("/subject-grade")
    public Result<List<QuestionResponse>> listBySubjectAndGrade(
            @RequestParam String subjectCode, @RequestParam Integer grade) {
        return Result.success(questionService.listBySubjectAndGrade(subjectCode, grade));
    }

    @GetMapping("/difficulty")
    public Result<List<QuestionResponse>> listByDifficulty(@RequestParam Integer difficulty) {
        return Result.success(questionService.listByDifficulty(difficulty));
    }

    @GetMapping("/type")
    public Result<List<QuestionResponse>> listByType(@RequestParam String type) {
        return Result.success(questionService.listByType(type));
    }

    @PostMapping("/create")
    @SaCheckPermission("edu:question:create")
    public Result<QuestionResponse> create(@Valid @RequestBody QuestionRequest request) {
        return Result.success(questionService.create(request));
    }

    @PostMapping("/update")
    @SaCheckPermission("edu:question:edit")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody QuestionRequest request) {
        request.setId(id);
        questionService.update(request);
        return Result.success();
    }

    @PostMapping("/delete")
    @SaCheckPermission("edu:question:delete")
    public Result<Void> delete(@RequestParam Long id) {
        questionService.deleteById(id);
        return Result.success();
    }
}
