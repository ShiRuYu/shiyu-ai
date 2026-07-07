package com.shiyu.ai.education.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.dataobject.education.ChapterDO;
import com.shiyu.ai.education.service.ChapterService;
import com.shiyu.ai.education.dto.ChapterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "章节管理")
@RestController
@RequestMapping("/edu/chapter")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;

    @GetMapping("/detail")
    @Operation(summary = "获取章节详情")
    public Result<ChapterResponse> getById(@RequestParam Long id) {
        ChapterDO chapter = chapterService.getById(id);
        return Result.success(toResponse(chapter));
    }

    @GetMapping("/textbook")
    @Operation(summary = "获取教材所有章节")
    public Result<List<ChapterResponse>> listByTextbookId(@RequestParam Long textbookId) {
        List<ChapterDO> chapters = chapterService.listByTextbookId(textbookId);
        return Result.success(chapters.stream().map(c -> toResponse(c)).toList());
    }

    @GetMapping("/textbook-tree")
    @Operation(summary = "获取教材章节树")
    public Result<List<ChapterResponse>> getChapterTree(@RequestParam Long textbookId) {
        List<ChapterDO> roots = chapterService.listRootChapters(textbookId);
        return Result.success(roots.stream().map(c -> toTreeResponse(c)).toList());
    }

    @PostMapping("/create")
    @Operation(summary = "创建章节")
    public Result<ChapterResponse> create(@Valid @RequestBody ChapterDO chapter) {
        ChapterDO created = chapterService.create(chapter);
        return Result.success(toResponse(created));
    }

    @PostMapping("/update")
    @Operation(summary = "更新章节")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody ChapterDO chapter) {
        chapter.setId(id);
        chapterService.update(chapter);
        return Result.success();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除章节")
    public Result<Void> delete(@RequestParam Long id) {
        chapterService.deleteById(id);
        return Result.success();
    }

    private ChapterResponse toResponse(ChapterDO chapter) {
        if (chapter == null) return null;
        return new ChapterResponse(chapter.getId(), chapter.getTextbookId(), chapter.getParentId(),
                chapter.getName(), chapter.getChapterOrder(), null);
    }

    private ChapterResponse toTreeResponse(ChapterDO chapter) {
        if (chapter == null) return null;
        List<ChapterDO> children = chapterService.listByParentId(chapter.getId());
        List<ChapterResponse> childResponses = children.stream().map(this::toTreeResponse).toList();
        return new ChapterResponse(chapter.getId(), chapter.getTextbookId(), chapter.getParentId(),
                chapter.getName(), chapter.getChapterOrder(), childResponses);
    }
}
