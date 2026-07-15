package com.shiyu.ai.education.controller;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.education.dto.TextbookResponse;
import com.shiyu.ai.education.request.TextbookRequest;
import com.shiyu.ai.education.service.TextbookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/textbook")
@RequiredArgsConstructor
public class TextbookController {

    private final TextbookService textbookService;

    @GetMapping("/detail")
    public Result<TextbookResponse> getById(@RequestParam Long id) {
        return Result.success(textbookService.getById(id));
    }

    @GetMapping("/list")
    public Result<PageData<TextbookResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(textbookService.page(pageNum, pageSize));
    }

    @GetMapping("/subject-grade")
    public Result<List<TextbookResponse>> listBySubjectAndGrade(
            @RequestParam String subjectCode, @RequestParam Integer grade) {
        return Result.success(textbookService.listBySubjectAndGrade(subjectCode, grade));
    }

    @PostMapping("/create")
    public Result<TextbookResponse> create(@Valid @RequestBody TextbookRequest request) {
        return Result.success(textbookService.create(request));
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody TextbookRequest request) {
        request.setId(id);
        textbookService.update(request);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        textbookService.deleteById(id);
        return Result.success();
    }
}
