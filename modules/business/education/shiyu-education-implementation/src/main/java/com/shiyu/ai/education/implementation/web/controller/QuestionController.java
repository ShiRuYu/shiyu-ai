package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.QuestionService;
import com.shiyu.ai.education.implementation.web.dto.QuestionResponse;
import com.shiyu.ai.education.implementation.web.request.QuestionRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code QuestionController} 是教育模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/question")
@RequiredArgsConstructor
@SaCheckPermission("edu:question:list")
public class QuestionController {

    /**
     * 题目服务，表示当前对象中的对应属性。
     */
    private final QuestionService questionService;

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @param pageNum 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/list")
    public Result<PageData<QuestionResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(
                questionService.page(ActorContextHttpAdapter.currentActor(), pageNum, pageSize));
    }

    /**
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/detail")
    public Result<QuestionResponse> getById(@RequestParam Long id) {
        return Result.success(questionService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * {@code listBySubjectAndGrade} 查询并返回当前操作所需的数据。
     *
     * @param subjectCode 参数值，用于执行当前操作。
     * @param grade 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/subject-grade")
    public Result<List<QuestionResponse>> listBySubjectAndGrade(
            @RequestParam String subjectCode, @RequestParam Integer grade) {
        return Result.success(
                questionService.listBySubjectAndGrade(
                        ActorContextHttpAdapter.currentActor(), subjectCode, grade));
    }

    /**
     * {@code listByDifficulty} 查询并返回当前操作所需的数据。
     *
     * @param difficulty 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/difficulty")
    public Result<List<QuestionResponse>> listByDifficulty(@RequestParam Integer difficulty) {
        return Result.success(
                questionService.listByDifficulty(
                        ActorContextHttpAdapter.currentActor(), difficulty));
    }

    /**
     * {@code listByType} 查询并返回当前操作所需的数据。
     *
     * @param type 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/type")
    public Result<List<QuestionResponse>> listByType(@RequestParam String type) {
        return Result.success(
                questionService.listByType(ActorContextHttpAdapter.currentActor(), type));
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/create")
    @SaCheckPermission("edu:question:create")
    public Result<QuestionResponse> create(@Valid @RequestBody QuestionRequest request) {
        return Result.success(
                questionService.create(ActorContextHttpAdapter.currentActor(), request));
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
    @SaCheckPermission("edu:question:edit")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody QuestionRequest request) {
        request.setId(id);
        questionService.update(ActorContextHttpAdapter.currentActor(), request);
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
    @SaCheckPermission("edu:question:delete")
    public Result<Void> delete(@RequestParam Long id) {
        questionService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
