package com.shiyu.ai.education.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.dataobject.education.ReviewTaskDO;
import com.shiyu.ai.education.dto.CompleteReviewRequest;
import com.shiyu.ai.education.dto.ReviewTaskResponse;
import com.shiyu.ai.education.review.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "复习管理")
@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{id}")
    @Operation(summary = "获取复习任务详情")
    public Result<ReviewTaskResponse> getById(@PathVariable Long id) {
        ReviewTaskDO task = reviewService.getById(id);
        return Result.success(toResponse(task));
    }

    @GetMapping("/today/{studentId}")
    @Operation(summary = "获取今日复习任务")
    public Result<List<ReviewTaskResponse>> listTodayTasks(@PathVariable Long studentId) {
        List<ReviewTaskDO> tasks = reviewService.listTodayTasks(studentId);
        return Result.success(tasks.stream().map(this::toResponse).toList());
    }

    @GetMapping("/student/{studentId}/status/{status}")
    @Operation(summary = "根据状态获取复习任务")
    public Result<List<ReviewTaskResponse>> listByStatus(
            @PathVariable Long studentId, @PathVariable String status) {
        List<ReviewTaskDO> tasks = reviewService.listByStudentAndStatus(studentId, status);
        return Result.success(tasks.stream().map(this::toResponse).toList());
    }

    @PostMapping
    @Operation(summary = "创建复习任务")
    public Result<ReviewTaskResponse> create(@RequestBody ReviewTaskDO task) {
        ReviewTaskDO created = reviewService.create(task);
        return Result.success(toResponse(created));
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "完成复习任务")
    public Result<Void> complete(@PathVariable Long id, @RequestBody CompleteReviewRequest request) {
        ReviewTaskDO task = reviewService.getById(id);
        if (task != null) {
            task.setStatus("COMPLETED");
            task.setResultScore(request.resultScore());
            task.setCompletedAt(java.time.LocalDateTime.now());
            reviewService.update(task);
        }
        return Result.success();
    }

    private ReviewTaskResponse toResponse(ReviewTaskDO task) {
        if (task == null) return null;
        return new ReviewTaskResponse(
                task.getId(), task.getStudentId(), task.getKnowledgeId(),
                null, task.getReviewRound(),
                task.getReviewDate() != null ? task.getReviewDate().toString() : null,
                task.getStatus(), null);
    }
}
