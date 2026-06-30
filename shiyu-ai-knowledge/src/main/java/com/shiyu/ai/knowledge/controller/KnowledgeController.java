package com.shiyu.ai.knowledge.controller;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.knowledge.domain.RelationType;
import com.shiyu.ai.knowledge.dto.CreateKnowledgeRequest;
import com.shiyu.ai.knowledge.dto.KnowledgePageQuery;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.dto.UpdateKnowledgeRequest;
import com.shiyu.ai.knowledge.graph.KnowledgeGraph;
import com.shiyu.ai.knowledge.path.LearningPathService;
import com.shiyu.ai.knowledge.search.KnowledgeSearchService;
import com.shiyu.ai.knowledge.search.SearchResult;
import com.shiyu.ai.knowledge.service.KnowledgeRelationService;
import com.shiyu.ai.knowledge.service.KnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识点管理")
@Validated
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final KnowledgeRelationService relationService;
    private final LearningPathService learningPathService;
    private final KnowledgeGraph knowledgeGraph;
    private final KnowledgeSearchService knowledgeSearchService;

    @GetMapping("/{id}")
    @Operation(summary = "获取知识点详情")
    public Result<KnowledgeResponse> getById(@PathVariable Long id) {
        return Result.success(knowledgeService.getById(id));
    }

    @GetMapping
    @Operation(summary = "分页查询知识点")
    public Result<PageData<KnowledgeResponse>> page(@Valid KnowledgePageQuery query) {
        return Result.success(knowledgeService.page(query));
    }

    @GetMapping("/{id}/graph")
    @Operation(summary = "获取知识图谱邻域")
    public Result<KnowledgeResponse> getGraph(@PathVariable Long id) {
        return Result.success(knowledgeService.getById(id));
    }

    @GetMapping("/{id}/path")
    @Operation(summary = "获取学习路径")
    public Result<List<Long>> getLearningPath(@PathVariable Long id) {
        return Result.success(learningPathService.generatePath(id));
    }

    @GetMapping("/{id}/prerequisites")
    @Operation(summary = "获取缺失前置知识")
    public Result<List<Long>> getMissingPrerequisites(@PathVariable Long id,
                                                      @RequestParam(required = false, defaultValue = "") Set<Long> masteredIds) {
        return Result.success(learningPathService.findMissingPrerequisites(id, masteredIds));
    }

    @GetMapping("/{id}/prerequisites-list")
    @Operation(summary = "获取前置知识点列表")
    public Result<List<KnowledgeResponse>> getPrerequisites(@PathVariable Long id) {
        return Result.success(relationService.getPrerequisites(id));
    }

    @GetMapping("/{id}/subsequent-list")
    @Operation(summary = "获取后续知识点列表")
    public Result<List<KnowledgeResponse>> getSubsequent(@PathVariable Long id) {
        return Result.success(relationService.getSubsequent(id));
    }

    @PostMapping
    @Operation(summary = "新增知识点")
    public Result<KnowledgeResponse> create(@RequestBody @Valid CreateKnowledgeRequest request) {
        return Result.success(knowledgeService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改知识点")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid UpdateKnowledgeRequest request) {
        knowledgeService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除知识点")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeService.delete(id);
        return Result.success();
    }

    @PostMapping("/relation")
    @Operation(summary = "新增知识点关系")
    public Result<Void> addRelation(@RequestParam Long sourceId,
                                    @RequestParam Long targetId,
                                    @RequestParam RelationType type,
                                    @RequestParam(required = false, defaultValue = "1.0") Double weight) {
        relationService.addRelation(sourceId, targetId, type, weight);
        return Result.success();
    }

    @DeleteMapping("/relation")
    @Operation(summary = "删除知识点关系")
    public Result<Void> removeRelation(@RequestParam Long sourceId,
                                       @RequestParam Long targetId,
                                       @RequestParam RelationType type) {
        relationService.removeRelation(sourceId, targetId, type);
        return Result.success();
    }

    @PostMapping("/reload")
    @Operation(summary = "重新加载知识图谱")
    public Result<Void> reloadGraph() {
        knowledgeGraph.reload();
        return Result.success();
    }
}
