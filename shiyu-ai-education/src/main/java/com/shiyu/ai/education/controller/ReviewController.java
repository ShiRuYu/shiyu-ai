package com.shiyu.ai.education.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.dataobject.education.ReviewTaskDO;
import com.shiyu.ai.education.dto.CompleteReviewRequest;
import com.shiyu.ai.education.dto.ReviewTaskResponse;
import com.shiyu.ai.education.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "复习管理")
@RestController
@RequestMapping("/edu/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/detail")
    @Operation(summary = "获取复习任务详情")
    public Result<ReviewTaskResponse> getById(@RequestParam Long id) {
        ReviewTaskDO task = reviewService.getById(id);
        return Result.success(toResponse(task));
    }

    @GetMapping("/today")
    @Operation(summary = "获取今日复习任务")
    public Result<List<ReviewTaskResponse>> listTodayTasks(@RequestParam Long studentId) {
        List<ReviewTaskDO> tasks = reviewService.listTodayTasks(studentId);
        return Result.success(tasks.stream().map(this::toResponse).toList());
    }

    @GetMapping("/list")
    @Operation(summary = "根据状态获取复习任务")
    public Result<List<ReviewTaskResponse>> listByStatus(
            @RequestParam Long studentId, @RequestParam String status) {
        List<ReviewTaskDO> tasks = reviewService.listByStudentAndStatus(studentId, status);
        return Result.success(tasks.stream().map(this::toResponse).toList());
    }

    @PostMapping("/create")
    @Operation(summary = "创建复习任务")
    public Result<ReviewTaskResponse> create(@Valid @RequestBody ReviewTaskDO task) {
        ReviewTaskDO created = reviewService.create(task);
        return Result.success(toResponse(created));
    }

    @PostMapping("/complete")
    @Operation(summary = "完成复习任务")
    public Result<Void> complete(@RequestParam Long id, @Valid @RequestBody CompleteReviewRequest request) {
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
