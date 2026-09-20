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
 * 处理 章节 相关的 Web 请求，并将请求转换为应用服务调用。
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
     * 查询 章节 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param detail 用于完成本次业务处理的 detail 参数。
     */
    @GetMapping("/detail")
    public Result<ChapterResponse> getById(@RequestParam Long id) {
        return Result.success(chapterService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * 查询 章节 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param textbook 用于完成本次业务处理的 textbook 参数。
     */
    @GetMapping("/textbook")
    public Result<List<ChapterResponse>> listByTextbookId(@RequestParam Long textbookId) {
        return Result.success(
                chapterService.listByTextbookId(
                        ActorContextHttpAdapter.currentActor(), textbookId));
    }

    /**
     * 查询 章节 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tree 用于完成本次业务处理的 tree 参数。
     */
    @GetMapping("/tree")
    public Result<List<ChapterResponse>> getChapterTree(@RequestParam Long textbookId) {
        return Result.success(
                chapterService.listRootChapters(
                        ActorContextHttpAdapter.currentActor(), textbookId));
    }

    /**
     * 查询 章节 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param children 用于完成本次业务处理的 children 参数。
     */
    @GetMapping("/children")
    public Result<List<ChapterResponse>> listByParentId(@RequestParam Long parentId) {
        return Result.success(
                chapterService.listByParentId(ActorContextHttpAdapter.currentActor(), parentId));
    }

    /**
     * 执行 章节 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param create 用于完成本次业务处理的 create 参数。
     */
    @PostMapping("/create")
    @SaCheckPermission("edu:chapter:create")
    public Result<ChapterResponse> create(@Valid @RequestBody ChapterRequest request) {
        return Result.success(
                chapterService.create(ActorContextHttpAdapter.currentActor(), request));
    }

    /**
     * 执行 章节 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param update 用于完成本次业务处理的 update 参数。
     */
    @PostMapping("/update")
    @SaCheckPermission("edu:chapter:edit")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody ChapterRequest request) {
        chapterService.update(ActorContextHttpAdapter.currentActor(), id, request);
        return Result.success();
    }

    /**
     * 执行 章节 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param delete 用于完成本次业务处理的 delete 参数。
     */
    @PostMapping("/delete")
    @SaCheckPermission("edu:chapter:delete")
    public Result<Void> delete(@RequestParam Long id) {
        chapterService.delete(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }

    /**
     * 查询 章节 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     */
    @GetMapping("/knowledge/list")
    public Result<List<Long>> listKnowledgeIds(@RequestParam Long chapterId) {
        return Result.success(
                chapterService.listKnowledgeIds(ActorContextHttpAdapter.currentActor(), chapterId));
    }

    /**
     * 执行 章节 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param bind 用于完成本次业务处理的 bind 参数。
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
