package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.ReviewService;
import com.shiyu.ai.education.implementation.web.dto.CompleteReviewRequest;
import com.shiyu.ai.education.implementation.web.dto.ReviewTaskResponse;
import com.shiyu.ai.education.implementation.web.request.ReviewRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 处理 复习 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    /**
     * reviewService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ReviewService reviewService;

    /**
     * 查询 复习 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param detail 用于完成本次业务处理的 detail 参数。
     */
    @SaCheckPermission("edu:review:list")
    @GetMapping("/{id}")
    public Result<ReviewTaskResponse> getById(@PathVariable Long id) {
        return Result.success(reviewService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * 查询 复习 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param today 用于完成本次业务处理的 today 参数。
     */
    @SaCheckPermission("edu:review:list")
    @GetMapping("/today")
    public Result<List<ReviewTaskResponse>> listTodayTasks(@RequestParam Long studentId) {
        return Result.success(
                reviewService.listTodayTasks(ActorContextHttpAdapter.currentActor(), studentId));
    }

    /**
     * 查询 复习 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     */
    @SaCheckPermission("edu:review:list")
    @GetMapping
    public Result<List<ReviewTaskResponse>> list(
            @RequestParam Long studentId, @RequestParam Integer status) {
        return Result.success(
                reviewService.listByStudentAndStatus(
                        ActorContextHttpAdapter.currentActor(), studentId, status));
    }

    /**
     * 执行 复习 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param create 用于完成本次业务处理的 create 参数。
     */
    @PostMapping
    @SaCheckPermission("edu:review:list")
    public Result<ReviewTaskResponse> create(@Valid @RequestBody ReviewRequest request) {
        return Result.success(
                reviewService.create(ActorContextHttpAdapter.currentActor(), request));
    }

    /**
     * 执行 复习 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param update 用于完成本次业务处理的 update 参数。
     */
    @SaCheckPermission("edu:review:list")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        request.setId(id);
        reviewService.update(ActorContextHttpAdapter.currentActor(), request);
        return Result.success();
    }

    /**
     * 执行 复习 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param complete 用于完成本次业务处理的 complete 参数。
     */
    @PostMapping("/complete")
    @SaCheckPermission("edu:review:list")
    public Result<Void> complete(
            @RequestParam Long id, @Valid @RequestBody CompleteReviewRequest request) {
        reviewService.complete(ActorContextHttpAdapter.currentActor(), id, request.resultScore());
        return Result.success();
    }

    /**
     * 执行 复习 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param delete 用于完成本次业务处理的 delete 参数。
     */
    @SaCheckPermission("edu:review:list")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reviewService.delete(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
