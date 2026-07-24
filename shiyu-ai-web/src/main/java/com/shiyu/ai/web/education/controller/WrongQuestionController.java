package com.shiyu.ai.web.education.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.education.dto.WrongQuestionResponse;
import com.shiyu.ai.education.request.WrongQuestionRequest;
import com.shiyu.ai.education.service.WrongQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.shiyu.ai.education.dto.WrongQuestionResponse;

@Slf4j
@RestController
@RequestMapping("/wrong-question")
@RequiredArgsConstructor
@SaCheckPermission("edu:wrong-question")
public class WrongQuestionController {

    private final WrongQuestionService wrongQuestionService;

    @GetMapping("/detail")
    public Result<WrongQuestionResponse> getById(@RequestParam Long id) {
        return Result.success(wrongQuestionService.getById(id));
    }

    @GetMapping("/student")
    public Result<List<WrongQuestionResponse>> listByStudentId(@RequestParam Long studentId) {
        return Result.success(wrongQuestionService.listByStudentId(studentId));
    }

    @PostMapping("/create")
    public Result<WrongQuestionResponse> create(@Valid @RequestBody WrongQuestionRequest request) {
        return Result.success(wrongQuestionService.create(request));
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody WrongQuestionRequest request) {
        request.setId(id);
        wrongQuestionService.update(request);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        wrongQuestionService.deleteById(id);
        return Result.success();
    }
}
