package com.shiyu.ai.web.knowledge;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.knowledge.graph.KnowledgeGraph;
import com.shiyu.ai.knowledge.search.KnowledgeSearchService;
import com.shiyu.ai.knowledge.task.IndexRebuildTask;
import com.shiyu.ai.knowledge.task.RebuildStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/knowledge/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识库管理（运维）")
@SaCheckPermission("knowledge:index:rebuild")
public class AdminController {

    private final KnowledgeGraph knowledgeGraph;
    private final KnowledgeSearchService knowledgeSearchService;
    private final IndexRebuildTask indexRebuildTask;

    @PostMapping("/graph/reload")
    @Operation(summary = "重新加载知识图谱")
    public Result<Void> reloadGraph() {
        knowledgeGraph.reload();
        return Result.success();
    }

    @PostMapping("/index/rebuild")
    @Operation(summary = "异步重建知识点向量索引")
    public Result<String> rebuildIndex() {
        String taskId = indexRebuildTask.createTask();
        indexRebuildTask.submitRebuildTask(taskId);
        return Result.success(taskId);
    }

    @GetMapping("/index/rebuild-status")
    @Operation(summary = "查询索引重建任务状态")
    public Result<RebuildStatus> getRebuildStatus(@RequestParam String taskId) {
        RebuildStatus status = indexRebuildTask.getTaskStatus(taskId);
        if (status == null) {
            return Result.fail("任务不存在");
        }
        return Result.success(status);
    }

    @GetMapping("/index/rebuild-tasks")
    @Operation(summary = "查询所有索引重建任务")
    public Result<List<RebuildStatus>> getAllRebuildTasks() {
        return Result.success(indexRebuildTask.getAllTasks());
    }

    @PostMapping("/index/clear")
    @Operation(summary = "清理知识点向量索引")
    public Result<Void> clearIndex() {
        knowledgeSearchService.clearIndex();
        return Result.success();
    }
}
