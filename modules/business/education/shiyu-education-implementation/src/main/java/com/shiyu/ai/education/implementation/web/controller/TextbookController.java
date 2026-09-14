package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.TextbookService;
import com.shiyu.ai.education.implementation.web.dto.TextbookResponse;
import com.shiyu.ai.education.implementation.web.request.TextbookRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code TextbookController} 是教育模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/textbook")
@RequiredArgsConstructor
@SaCheckPermission("edu:textbook:list")
public class TextbookController {

    /**
     * textbookService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final TextbookService textbookService;

    /**
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/detail")
    public Result<TextbookResponse> getById(@RequestParam Long id) {
        return Result.success(textbookService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @param pageNum 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/list")
    public Result<PageData<TextbookResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(
                textbookService.page(ActorContextHttpAdapter.currentActor(), pageNum, pageSize));
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
    public Result<List<TextbookResponse>> listBySubjectAndGrade(
            @RequestParam String subjectCode, @RequestParam Integer grade) {
        return Result.success(
                textbookService.listBySubjectAndGrade(
                        ActorContextHttpAdapter.currentActor(), subjectCode, grade));
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/create")
    @SaCheckPermission("edu:textbook:create")
    public Result<TextbookResponse> create(@Valid @RequestBody TextbookRequest request) {
        return Result.success(
                textbookService.create(ActorContextHttpAdapter.currentActor(), request));
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
    @SaCheckPermission("edu:textbook:edit")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody TextbookRequest request) {
        request.setId(id);
        textbookService.update(ActorContextHttpAdapter.currentActor(), request);
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
    @SaCheckPermission("edu:textbook:delete")
    public Result<Void> delete(@RequestParam Long id) {
        textbookService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
