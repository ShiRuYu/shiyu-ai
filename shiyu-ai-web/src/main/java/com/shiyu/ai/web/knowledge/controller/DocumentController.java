package com.shiyu.ai.web.knowledge.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
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
@RequestMapping("/knowledge/document")
@RequiredArgsConstructor
@Tag(name = "文档知识管理")
@SaCheckPermission("knowledge:document:list")
@Validated
public class DocumentController {

    private final DocumentKnowledgeService documentKnowledgeService;

    @GetMapping("/detail")
    @Operation(summary = "获取文档详情")
    public Result<DocumentKnowledgeService.KnowledgeDocumentVO> getById(@RequestParam Long id) {
        return Result.success(documentKnowledgeService.getById(id));
    }

    @GetMapping("/list")
    @Operation(summary = "搜索文档")
    public Result<List<DocumentKnowledgeService.KnowledgeDocumentVO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int topK) {
        return Result.success(documentKnowledgeService.search(keyword, topK));
    }

    @GetMapping("/knowledge")
    @Operation(summary = "根据知识点ID获取关联文档")
    public Result<List<DocumentKnowledgeService.KnowledgeDocumentVO>> getByKnowledge(
            @RequestParam Long knowledgeId) {
        return Result.success(documentKnowledgeService.searchByKnowledgeId(knowledgeId));
    }

    @PostMapping("/create")
    @Operation(summary = "新增文档")
    @SaCheckPermission("knowledge:document:upload")
    public Result<DocumentKnowledgeService.KnowledgeDocumentVO> create(
            @RequestBody @Valid DocumentKnowledgeService.CreateDocumentRequest request) {
        return Result.success(documentKnowledgeService.create(request));
    }

    @PostMapping("/update")
    @Operation(summary = "修改文档")
    @SaCheckPermission("knowledge:document:upload")
    public Result<Void> update(@RequestParam Long id,
                               @RequestBody @Valid DocumentKnowledgeService.UpdateDocumentRequest request) {
        documentKnowledgeService.update(id, request);
        return Result.success();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除文档")
    @SaCheckPermission("knowledge:document:delete")
    public Result<Void> delete(@RequestParam Long id) {
        documentKnowledgeService.delete(id);
        return Result.success();
    }
}
