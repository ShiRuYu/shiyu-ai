package com.shiyu.ai.education.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.education.dto.QuestionResponse;
import com.shiyu.ai.education.dto.QuestionResponse;
import com.shiyu.ai.education.request.QuestionRequest;
import com.shiyu.ai.education.service.QuestionService;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
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
        return Result.success(questionService.page(ActorContextHttpAdapter.currentActor(), pageNum, pageSize));
    }

    @GetMapping("/detail")
    public Result<QuestionResponse> getById(@RequestParam Long id) {
        return Result.success(questionService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    @GetMapping("/subject-grade")
    public Result<List<QuestionResponse>> listBySubjectAndGrade(
            @RequestParam String subjectCode, @RequestParam Integer grade) {
        return Result.success(questionService.listBySubjectAndGrade(ActorContextHttpAdapter.currentActor(), subjectCode, grade));
    }

    @GetMapping("/difficulty")
    public Result<List<QuestionResponse>> listByDifficulty(@RequestParam Integer difficulty) {
        return Result.success(questionService.listByDifficulty(ActorContextHttpAdapter.currentActor(), difficulty));
    }

    @GetMapping("/type")
    public Result<List<QuestionResponse>> listByType(@RequestParam String type) {
        return Result.success(questionService.listByType(ActorContextHttpAdapter.currentActor(), type));
    }

    @PostMapping("/create")
    @SaCheckPermission("edu:question:create")
    public Result<QuestionResponse> create(@Valid @RequestBody QuestionRequest request) {
        return Result.success(questionService.create(ActorContextHttpAdapter.currentActor(), request));
    }

    @PostMapping("/update")
    @SaCheckPermission("edu:question:edit")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody QuestionRequest request) {
        request.setId(id);
        questionService.update(ActorContextHttpAdapter.currentActor(), request);
        return Result.success();
    }

    @PostMapping("/delete")
    @SaCheckPermission("edu:question:delete")
    public Result<Void> delete(@RequestParam Long id) {
        questionService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
