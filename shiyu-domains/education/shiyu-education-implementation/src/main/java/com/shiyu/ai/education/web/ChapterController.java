package com.shiyu.ai.education.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.education.dto.ChapterResponse;
import com.shiyu.ai.education.request.ChapterRequest;
import com.shiyu.ai.education.service.ChapterService;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/chapter")
@RequiredArgsConstructor
@SaCheckPermission("edu:chapter:list")
public class ChapterController {

    private final ChapterService chapterService;

    @GetMapping("/detail")
    public Result<ChapterResponse> getById(@RequestParam Long id) {
        return Result.success(chapterService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    @GetMapping("/textbook")
    public Result<List<ChapterResponse>> listByTextbookId(@RequestParam Long textbookId) {
        return Result.success(chapterService.listByTextbookId(ActorContextHttpAdapter.currentActor(), textbookId));
    }

    @GetMapping("/tree")
    public Result<List<ChapterResponse>> getChapterTree(@RequestParam Long textbookId) {
        return Result.success(chapterService.listRootChapters(ActorContextHttpAdapter.currentActor(), textbookId));
    }

    @GetMapping("/children")
    public Result<List<ChapterResponse>> listByParentId(@RequestParam Long parentId) {
        return Result.success(chapterService.listByParentId(ActorContextHttpAdapter.currentActor(), parentId));
    }

    @PostMapping("/create")
    @SaCheckPermission("edu:chapter:create")
    public Result<ChapterResponse> create(@Valid @RequestBody ChapterRequest request) {
        return Result.success(chapterService.create(ActorContextHttpAdapter.currentActor(), request));
    }

    @PostMapping("/update")
    @SaCheckPermission("edu:chapter:edit")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody ChapterRequest request) {
        chapterService.update(ActorContextHttpAdapter.currentActor(), id, request);
        return Result.success();
    }

    @PostMapping("/delete")
    @SaCheckPermission("edu:chapter:delete")
    public Result<Void> delete(@RequestParam Long id) {
        chapterService.delete(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }

    @GetMapping("/knowledge/list")
    public Result<List<Long>> listKnowledgeIds(@RequestParam Long chapterId) {
        return Result.success(chapterService.listKnowledgeIds(ActorContextHttpAdapter.currentActor(), chapterId));
    }

    @PostMapping("/knowledge/bind")
    @SaCheckPermission("edu:chapter:edit")
    public Result<Void> replaceKnowledgeIds(@RequestParam Long chapterId,
                                            @RequestBody List<Long> knowledgeIds) {
        chapterService.replaceKnowledgeIds(ActorContextHttpAdapter.currentActor(), chapterId, knowledgeIds);
        return Result.success();
    }
}
