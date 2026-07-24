package com.shiyu.ai.web.education.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.education.dto.ReviewTaskResponse;
import com.shiyu.ai.education.dto.CompleteReviewRequest;
import com.shiyu.ai.education.request.ReviewRequest;
import com.shiyu.ai.education.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.shiyu.ai.education.dto.ReviewTaskResponse;

@Slf4j
@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
@SaCheckPermission("edu:review:list")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/detail")
    public Result<ReviewTaskResponse> getById(@RequestParam Long id) {
        return Result.success(reviewService.getById(id));
    }

    @GetMapping("/today")
    public Result<List<ReviewTaskResponse>> listTodayTasks(@RequestParam Long studentId) {
        return Result.success(reviewService.listTodayTasks(studentId));
    }

    @GetMapping("/list")
    public Result<List<ReviewTaskResponse>> list(@RequestParam Long studentId, @RequestParam Integer status) {
        return Result.success(reviewService.listByStudentAndStatus(studentId, status));
    }

    @PostMapping("/create")
    @SaCheckPermission("edu:review:list")
    public Result<ReviewTaskResponse> create(@Valid @RequestBody ReviewRequest request) {
        return Result.success(reviewService.create(request));
    }

    @PostMapping("/complete")
    @SaCheckPermission("edu:review:list")
    public Result<Void> complete(@RequestParam Long id, @Valid @RequestBody CompleteReviewRequest request) {
        return Result.success();
    }
}
