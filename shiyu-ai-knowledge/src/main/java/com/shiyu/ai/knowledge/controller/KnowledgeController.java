package com.shiyu.ai.knowledge.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.knowledge.domain.RelationType;
import com.shiyu.ai.knowledge.dto.CreateKnowledgeRequest;
import com.shiyu.ai.knowledge.dto.KnowledgeGraphResponse;
import com.shiyu.ai.knowledge.dto.KnowledgePageQuery;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.request.KnowledgeRelationRequest;
import com.shiyu.ai.knowledge.request.KnowledgeSearchRequest;
import com.shiyu.ai.knowledge.dto.UpdateKnowledgeRequest;
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
@RequestMapping("/knowledge/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识点管理")
@SaCheckPermission("knowledge:list")
@Validated
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final KnowledgeRelationService relationService;
    private final LearningPathService learningPathService;
    private final KnowledgeSearchService knowledgeSearchService;

    @GetMapping("/detail")
    @Operation(summary = "获取知识点详情")
    public Result<KnowledgeResponse> getById(@RequestParam Long id) {
        return Result.success(knowledgeService.getById(id));
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询知识点")
    public Result<PageData<KnowledgeResponse>> page(@Valid KnowledgePageQuery query) {
        return Result.success(knowledgeService.page(query));
    }

    @GetMapping("/graph")
    @Operation(summary = "获取知识图谱邻域")
    public Result<KnowledgeGraphResponse> getGraph(@RequestParam Long id) {
        return Result.success(knowledgeService.getGraph(id));
    }

    @GetMapping("/path")
    @Operation(summary = "获取学习路径")
    public Result<List<Long>> getLearningPath(@RequestParam Long id) {
        return Result.success(learningPathService.generatePath(id));
    }

    @GetMapping("/prerequisites")
    @Operation(summary = "获取缺失前置知识")
    public Result<List<Long>> getMissingPrerequisites(@RequestParam Long id,
                                                      @RequestParam(required = false, defaultValue = "") Set<Long> masteredIds) {
        return Result.success(learningPathService.findMissingPrerequisites(id, masteredIds));
    }

    @GetMapping("/prerequisites-list")
    @Operation(summary = "获取前置知识点列表")
    public Result<List<KnowledgeResponse>> getPrerequisites(@RequestParam Long id) {
        return Result.success(relationService.getPrerequisites(id));
    }

    @GetMapping("/subsequent-list")
    @Operation(summary = "获取后续知识点列表")
    public Result<List<KnowledgeResponse>> getSubsequent(@RequestParam Long id) {
        return Result.success(relationService.getSubsequent(id));
    }

    @PostMapping("/create")
    @Operation(summary = "新增知识点")
    @SaCheckPermission("knowledge:create")
    public Result<KnowledgeResponse> create(@RequestBody @Valid CreateKnowledgeRequest request) {
        return Result.success(knowledgeService.create(request));
    }

    @PostMapping("/update")
    @Operation(summary = "修改知识点")
    @SaCheckPermission("knowledge:edit")
    public Result<Void> update(@RequestParam Long id, @RequestBody @Valid UpdateKnowledgeRequest request) {
        knowledgeService.update(id, request);
        return Result.success();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除知识点")
    @SaCheckPermission("knowledge:delete")
    public Result<Void> delete(@RequestParam Long id) {
        knowledgeService.delete(id);
        return Result.success();
    }

    @PostMapping("/relation/create")
    @Operation(summary = "新增知识点关系")
    @SaCheckPermission("knowledge:relation")
    public Result<Void> addRelation(@RequestParam Long sourceId,
                                    @RequestParam Long targetId,
                                    @RequestParam RelationType type,
                                    @RequestParam(required = false, defaultValue = "1.0") Double weight) {
        relationService.addRelation(sourceId, targetId, type, weight);
        return Result.success();
    }

    @PostMapping("/relation/delete")
    @Operation(summary = "删除知识点关系")
    @SaCheckPermission("knowledge:relation")
    public Result<Void> removeRelation(@RequestParam Long sourceId,
                                       @RequestParam Long targetId,
                                       @RequestParam RelationType type) {
        relationService.removeRelation(sourceId, targetId, type);
        return Result.success();
    }

    @GetMapping("/search")
    @Operation(summary = "搜索知识点")
    public Result<List<SearchResult>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int topK) {
        return Result.success(knowledgeSearchService.search(query, topK));
    }
}
