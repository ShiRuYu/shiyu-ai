package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.ChapterService;
import com.shiyu.ai.education.implementation.web.dto.ChapterResponse;
import com.shiyu.ai.education.implementation.web.request.ChapterRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code ChapterController} 是教育模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/chapter")
@RequiredArgsConstructor
@SaCheckPermission("edu:chapter:list")
public class ChapterController {

    /**
     * 章节服务，表示当前对象中的对应属性。
     */
    private final ChapterService chapterService;

    /**
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/detail")
    public Result<ChapterResponse> getById(@RequestParam Long id) {
        return Result.success(chapterService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * {@code listByTextbookId} 查询并返回当前操作所需的数据。
     *
     * @param textbookId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/textbook")
    public Result<List<ChapterResponse>> listByTextbookId(@RequestParam Long textbookId) {
        return Result.success(
                chapterService.listByTextbookId(
                        ActorContextHttpAdapter.currentActor(), textbookId));
    }

    /**
     * {@code getChapterTree} 查询并返回当前操作所需的数据。
     *
     * @param textbookId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/tree")
    public Result<List<ChapterResponse>> getChapterTree(@RequestParam Long textbookId) {
        return Result.success(
                chapterService.listRootChapters(
                        ActorContextHttpAdapter.currentActor(), textbookId));
    }

    /**
     * {@code listByParentId} 查询并返回当前操作所需的数据。
     *
     * @param parentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/children")
    public Result<List<ChapterResponse>> listByParentId(@RequestParam Long parentId) {
        return Result.success(
                chapterService.listByParentId(ActorContextHttpAdapter.currentActor(), parentId));
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/create")
    @SaCheckPermission("edu:chapter:create")
    public Result<ChapterResponse> create(@Valid @RequestBody ChapterRequest request) {
        return Result.success(
                chapterService.create(ActorContextHttpAdapter.currentActor(), request));
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
    @SaCheckPermission("edu:chapter:edit")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody ChapterRequest request) {
        chapterService.update(ActorContextHttpAdapter.currentActor(), id, request);
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
    @SaCheckPermission("edu:chapter:delete")
    public Result<Void> delete(@RequestParam Long id) {
        chapterService.delete(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }

    /**
     * {@code listKnowledgeIds} 查询并返回当前操作所需的数据。
     *
     * @param chapterId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/knowledge/list")
    public Result<List<Long>> listKnowledgeIds(@RequestParam Long chapterId) {
        return Result.success(
                chapterService.listKnowledgeIds(ActorContextHttpAdapter.currentActor(), chapterId));
    }

    /**
     * {@code replaceKnowledgeIds} 执行当前类型定义的业务操作。
     *
     * @param chapterId 参数值，用于执行当前操作。
     * @param knowledgeIds 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/knowledge/bind")
    @SaCheckPermission("edu:chapter:edit")
    public Result<Void> replaceKnowledgeIds(
            @RequestParam Long chapterId, @RequestBody List<Long> knowledgeIds) {
        chapterService.replaceKnowledgeIds(
                ActorContextHttpAdapter.currentActor(), chapterId, knowledgeIds);
        return Result.success();
    }
}
