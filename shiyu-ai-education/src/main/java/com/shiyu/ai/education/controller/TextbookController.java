package com.shiyu.ai.education.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.dataobject.education.TextbookDO;
import com.shiyu.ai.education.dto.TextbookResponse;
import com.shiyu.ai.education.textbook.TextbookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "教材管理")
@RestController
@RequestMapping("/api/textbook")
@RequiredArgsConstructor
public class TextbookController {

    private final TextbookService textbookService;

    @GetMapping("/{id}")
    @Operation(summary = "获取教材详情")
    public Result<TextbookResponse> getById(@PathVariable Long id) {
        TextbookDO textbook = textbookService.getById(id);
        return Result.success(toResponse(textbook));
    }

    @GetMapping
    @Operation(summary = "获取所有教材")
    public Result<List<TextbookResponse>> listAll() {
        List<TextbookDO> textbooks = textbookService.listAll();
        return Result.success(textbooks.stream().map(this::toResponse).toList());
    }

    @GetMapping("/subject/{subjectCode}/grade/{grade}")
    @Operation(summary = "根据学科和年级获取教材")
    public Result<List<TextbookResponse>> listBySubjectAndGrade(
            @PathVariable String subjectCode, @PathVariable Integer grade) {
        List<TextbookDO> textbooks = textbookService.listBySubjectAndGrade(subjectCode, grade);
        return Result.success(textbooks.stream().map(this::toResponse).toList());
    }

    @PostMapping
    @Operation(summary = "创建教材")
    public Result<TextbookResponse> create(@Valid @RequestBody TextbookDO textbook) {
        TextbookDO created = textbookService.create(textbook);
        return Result.success(toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新教材")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TextbookDO textbook) {
        textbook.setId(id);
        textbookService.update(textbook);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除教材")
    public Result<Void> delete(@PathVariable Long id) {
        textbookService.deleteById(id);
        return Result.success();
    }

    private TextbookResponse toResponse(TextbookDO textbook) {
        if (textbook == null) return null;
        return new TextbookResponse(
                textbook.getId(), textbook.getName(), textbook.getSubjectCode(),
                textbook.getGrade(), textbook.getPublisher(), textbook.getIsbn());
    }
}
