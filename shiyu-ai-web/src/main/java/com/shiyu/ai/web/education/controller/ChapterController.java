package com.shiyu.ai.web.education.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.education.dto.ChapterResponse;
import com.shiyu.ai.education.service.ChapterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/chapter")
@RequiredArgsConstructor
@SaCheckPermission("edu:chapter:list")
public class ChapterController {

    private final ChapterService chapterService;

    @GetMapping("/detail")
    public Result<ChapterResponse> getById(@RequestParam Long id) {
        return Result.success(chapterService.getById(id));
    }

    @GetMapping("/textbook")
    public Result<List<ChapterResponse>> listByTextbookId(@RequestParam Long textbookId) {
        return Result.success(chapterService.listByTextbookId(textbookId));
    }

    @GetMapping("/tree")
    public Result<List<ChapterResponse>> getChapterTree(@RequestParam Long textbookId) {
        return Result.success(chapterService.listRootChapters(textbookId));
    }

    @GetMapping("/children")
    public Result<List<ChapterResponse>> listByParentId(@RequestParam Long parentId) {
        return Result.success(chapterService.listByParentId(parentId));
    }
}
