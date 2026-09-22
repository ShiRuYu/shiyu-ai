package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.api.Result;
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
 * 处理 题目 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/question")
@RequiredArgsConstructor
public class QuestionController {

    /**
     * 题目服务，表示当前对象中的对应属性。
     */
    private final QuestionService questionService;

    /**
     * 查询 题目 相关业务数据，并返回处理结果。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     * @return 返回 题目 相关操作生成的结果数据。
     */
    @SaCheckPermission("edu:question:list")
    @GetMapping
    public Result<PageData<QuestionResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(
                questionService.page(ActorContextHttpAdapter.currentActor(), pageNum, pageSize));
    }

    /**
     * 查询 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param detail 用于完成本次业务处理的 detail 参数。
     */
    @SaCheckPermission("edu:question:list")
    @GetMapping("/{id}")
    public Result<QuestionResponse> getById(@PathVariable Long id) {
        return Result.success(questionService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * 查询 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param grade 用于完成本次业务处理的 grade 参数。
     */
    @SaCheckPermission("edu:question:list")
    @GetMapping("/subject-grade")
    public Result<List<QuestionResponse>> listBySubjectAndGrade(
            @RequestParam String subjectCode, @RequestParam Integer grade) {
        return Result.success(
                questionService.listBySubjectAndGrade(
                        ActorContextHttpAdapter.currentActor(), subjectCode, grade));
    }

    /**
     * 查询 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param difficulty 用于完成本次业务处理的 difficulty 参数。
     */
    @SaCheckPermission("edu:question:list")
    @GetMapping("/difficulty")
    public Result<List<QuestionResponse>> listByDifficulty(@RequestParam Integer difficulty) {
        return Result.success(
                questionService.listByDifficulty(
                        ActorContextHttpAdapter.currentActor(), difficulty));
    }

    /**
     * 查询 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param type 用于完成本次业务处理的 type 参数。
     */
    @SaCheckPermission("edu:question:list")
    @GetMapping("/type")
    public Result<List<QuestionResponse>> listByType(@RequestParam String type) {
        return Result.success(
                questionService.listByType(ActorContextHttpAdapter.currentActor(), type));
    }

    /**
     * 执行 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param create 用于完成本次业务处理的 create 参数。
     */
    @PostMapping
    @SaCheckPermission("edu:question:create")
    public Result<QuestionResponse> create(@Valid @RequestBody QuestionRequest request) {
        return Result.success(
                questionService.create(ActorContextHttpAdapter.currentActor(), request));
    }

    /**
     * 执行 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param update 用于完成本次业务处理的 update 参数。
     */
    @PutMapping("/{id}")
    @SaCheckPermission("edu:question:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        request.setId(id);
        questionService.update(ActorContextHttpAdapter.currentActor(), request);
        return Result.success();
    }

    /**
     * 执行 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param delete 用于完成本次业务处理的 delete 参数。
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("edu:question:delete")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
