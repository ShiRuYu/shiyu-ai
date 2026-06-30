package com.shiyu.ai.knowledge.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.knowledge.service.DocumentKnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/knowledge/documents")
@RequiredArgsConstructor
@Tag(name = "文档知识管理")
@Validated
public class DocumentController {

    private final DocumentKnowledgeService documentKnowledgeService;

    @GetMapping("/{id}")
    @Operation(summary = "获取文档详情")
    public Result<DocumentKnowledgeService.KnowledgeDocumentVO> getById(@PathVariable Long id) {
        return Result.success(documentKnowledgeService.getById(id));
    }

    @GetMapping
    @Operation(summary = "搜索文档")
    public Result<List<DocumentKnowledgeService.KnowledgeDocumentVO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int topK) {
        return Result.success(documentKnowledgeService.search(keyword, topK));
    }

    @GetMapping("/by-knowledge/{knowledgeId}")
    @Operation(summary = "根据知识点ID获取关联文档")
    public Result<List<DocumentKnowledgeService.KnowledgeDocumentVO>> getByKnowledge(
            @PathVariable Long knowledgeId) {
        return Result.success(documentKnowledgeService.searchByKnowledgeId(knowledgeId));
    }

    @PostMapping
    @Operation(summary = "新增文档")
    public Result<DocumentKnowledgeService.KnowledgeDocumentVO> create(
            @RequestBody @Valid DocumentKnowledgeService.CreateDocumentRequest request) {
        return Result.success(documentKnowledgeService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改文档")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody @Valid DocumentKnowledgeService.UpdateDocumentRequest request) {
        documentKnowledgeService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文档")
    public Result<Void> delete(@PathVariable Long id) {
        documentKnowledgeService.delete(id);
        return Result.success();
    }
}
