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
 * 处理 Wrong 题目 相关的 Web 请求，并将请求转换为应用服务调用。
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
     * 查询 Wrong 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param detail 用于完成本次业务处理的 detail 参数。
     */
    @GetMapping("/detail")
    public Result<WrongQuestionResponse> getById(@RequestParam Long id) {
        return Result.success(
                wrongQuestionService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * 查询 Wrong 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param student 用于完成本次业务处理的 student 参数。
     */
    @GetMapping("/student")
    public Result<List<WrongQuestionResponse>> listByStudentId(@RequestParam Long studentId) {
        return Result.success(
                wrongQuestionService.listByStudentId(
                        ActorContextHttpAdapter.currentActor(), studentId));
    }

    /**
     * 执行 Wrong 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param create 用于完成本次业务处理的 create 参数。
     */
    @PostMapping("/create")
    public Result<WrongQuestionResponse> create(@Valid @RequestBody WrongQuestionRequest request) {
        return Result.success(
                wrongQuestionService.create(ActorContextHttpAdapter.currentActor(), request));
    }

    /**
     * 执行 Wrong 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param update 用于完成本次业务处理的 update 参数。
     */
    @PostMapping("/update")
    public Result<Void> update(
            @RequestParam Long id, @Valid @RequestBody WrongQuestionRequest request) {
        request.setId(id);
        wrongQuestionService.update(ActorContextHttpAdapter.currentActor(), request);
        return Result.success();
    }

    /**
     * 执行 Wrong 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param delete 用于完成本次业务处理的 delete 参数。
     */
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        wrongQuestionService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
