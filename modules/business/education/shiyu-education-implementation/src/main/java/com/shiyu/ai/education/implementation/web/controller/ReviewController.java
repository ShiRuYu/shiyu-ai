package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.Result;
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
 * {@code ReviewController} 是教育模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
@SaCheckPermission("edu:review:list")
public class ReviewController {

    /**
     * reviewService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ReviewService reviewService;

    /**
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/detail")
    public Result<ReviewTaskResponse> getById(@RequestParam Long id) {
        return Result.success(reviewService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * {@code listTodayTasks} 查询并返回当前操作所需的数据。
     *
     * @param studentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/today")
    public Result<List<ReviewTaskResponse>> listTodayTasks(@RequestParam Long studentId) {
        return Result.success(
                reviewService.listTodayTasks(ActorContextHttpAdapter.currentActor(), studentId));
    }

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @param studentId 参数值，用于执行当前操作。
     * @param status 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/list")
    public Result<List<ReviewTaskResponse>> list(
            @RequestParam Long studentId, @RequestParam Integer status) {
        return Result.success(
                reviewService.listByStudentAndStatus(
                        ActorContextHttpAdapter.currentActor(), studentId, status));
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/create")
    @SaCheckPermission("edu:review:list")
    public Result<ReviewTaskResponse> create(@Valid @RequestBody ReviewRequest request) {
        return Result.success(
                reviewService.create(ActorContextHttpAdapter.currentActor(), request));
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/update")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody ReviewRequest request) {
        request.setId(id);
        reviewService.update(ActorContextHttpAdapter.currentActor(), request);
        return Result.success();
    }

    /**
     * {@code complete} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/complete")
    @SaCheckPermission("edu:review:list")
    public Result<Void> complete(
            @RequestParam Long id, @Valid @RequestBody CompleteReviewRequest request) {
        reviewService.complete(ActorContextHttpAdapter.currentActor(), id, request.resultScore());
        return Result.success();
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        reviewService.delete(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
