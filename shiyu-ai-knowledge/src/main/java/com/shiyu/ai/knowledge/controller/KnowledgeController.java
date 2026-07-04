package com.shiyu.ai.knowledge.controller;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.knowledge.domain.RelationType;
import com.shiyu.ai.knowledge.dto.CreateKnowledgeRequest;
import com.shiyu.ai.knowledge.dto.KnowledgeGraphResponse;
import com.shiyu.ai.knowledge.dto.KnowledgePageQuery;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.dto.UpdateKnowledgeRequest;
import com.shiyu.ai.knowledge.graph.KnowledgeGraph;
import com.shiyu.ai.knowledge.path.LearningPathService;
import com.shiyu.ai.knowledge.search.KnowledgeSearchService;
import com.shiyu.ai.knowledge.search.SearchMode;
import com.shiyu.ai.knowledge.search.SearchResult;
import com.shiyu.ai.knowledge.service.KnowledgeRelationService;
import com.shiyu.ai.knowledge.service.KnowledgeService;
import com.shiyu.ai.knowledge.task.IndexRebuildTask;
import com.shiyu.ai.knowledge.task.RebuildStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识点管理")
@Validated
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final KnowledgeRelationService relationService;
    private final LearningPathService learningPathService;
    private final KnowledgeGraph knowledgeGraph;
    private final KnowledgeSearchService knowledgeSearchService;
    private final IndexRebuildTask indexRebuildTask;

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
    public Result<KnowledgeGraphResponse> getGraph(@PathVariable Long id) {
        return Result.success(knowledgeService.getGraph(id));
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

    @GetMapping("/search")
    @Operation(summary = "搜索知识点")
    public Result<List<SearchResult>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int topK,
            @RequestParam(defaultValue = "HYBRID") SearchMode mode) {
        return Result.success(knowledgeSearchService.search(query, topK, mode));
    }

    @GetMapping("/search/modes")
    @Operation(summary = "获取可用的搜索模式")
    public Result<Set<SearchMode>> getAvailableModes() {
        return Result.success(knowledgeSearchService.getAvailableModes());
    }

    @PostMapping("/rebuild-index")
    @Operation(summary = "异步重建知识点向量索引")
    public Result<String> rebuildIndex() {
        String taskId = indexRebuildTask.createTask();
        indexRebuildTask.submitRebuildTask(taskId);
        return Result.success(taskId);
    }

    @GetMapping("/rebuild-index/{taskId}")
    @Operation(summary = "查询索引重建任务状态")
    public Result<RebuildStatus> getRebuildStatus(@PathVariable String taskId) {
        RebuildStatus status = indexRebuildTask.getTaskStatus(taskId);
        if (status == null) {
            return Result.fail("任务不存在");
        }
        return Result.success(status);
    }

    @GetMapping("/rebuild-index")
    @Operation(summary = "查询所有索引重建任务")
    public Result<List<RebuildStatus>> getAllRebuildTasks() {
        return Result.success(indexRebuildTask.getAllTasks());
    }

    @DeleteMapping("/index")
    @Operation(summary = "清理知识点向量索引")
    public Result<Void> clearIndex() {
        knowledgeSearchService.clearIndex();
        return Result.success();
    }
}
