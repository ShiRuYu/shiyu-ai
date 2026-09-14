package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.WrongQuestionService;
import com.shiyu.ai.education.implementation.web.dto.WrongQuestionResponse;
import com.shiyu.ai.education.implementation.web.request.WrongQuestionRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code WrongQuestionController} 是教育模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/wrong-question")
@RequiredArgsConstructor
@SaCheckPermission("edu:wrong-question")
public class WrongQuestionController {

    /**
     * wrongQuestionService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final WrongQuestionService wrongQuestionService;

    /**
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/detail")
    public Result<WrongQuestionResponse> getById(@RequestParam Long id) {
        return Result.success(
                wrongQuestionService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * {@code listByStudentId} 查询并返回当前操作所需的数据。
     *
     * @param studentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/student")
    public Result<List<WrongQuestionResponse>> listByStudentId(@RequestParam Long studentId) {
        return Result.success(
                wrongQuestionService.listByStudentId(
                        ActorContextHttpAdapter.currentActor(), studentId));
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/create")
    public Result<WrongQuestionResponse> create(@Valid @RequestBody WrongQuestionRequest request) {
        return Result.success(
                wrongQuestionService.create(ActorContextHttpAdapter.currentActor(), request));
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
    public Result<Void> update(
            @RequestParam Long id, @Valid @RequestBody WrongQuestionRequest request) {
        request.setId(id);
        wrongQuestionService.update(ActorContextHttpAdapter.currentActor(), request);
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
        wrongQuestionService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
