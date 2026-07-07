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
@RequestMapping("/api/chapter")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;

    @GetMapping("/{id}")
    @Operation(summary = "获取章节详情")
    public Result<ChapterResponse> getById(@PathVariable Long id) {
        ChapterDO chapter = chapterService.getById(id);
        return Result.success(toResponse(chapter));
    }

    @GetMapping("/textbook/{textbookId}")
    @Operation(summary = "获取教材所有章节")
    public Result<List<ChapterResponse>> listByTextbookId(@PathVariable Long textbookId) {
        List<ChapterDO> chapters = chapterService.listByTextbookId(textbookId);
        return Result.success(chapters.stream().map(c -> toResponse(c)).toList());
    }

    @GetMapping("/textbook/{textbookId}/tree")
    @Operation(summary = "获取教材章节树")
    public Result<List<ChapterResponse>> getChapterTree(@PathVariable Long textbookId) {
        List<ChapterDO> roots = chapterService.listRootChapters(textbookId);
        return Result.success(roots.stream().map(c -> toTreeResponse(c)).toList());
    }

    @PostMapping
    @Operation(summary = "创建章节")
    public Result<ChapterResponse> create(@Valid @RequestBody ChapterDO chapter) {
        ChapterDO created = chapterService.create(chapter);
        return Result.success(toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新章节")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ChapterDO chapter) {
        chapter.setId(id);
        chapterService.update(chapter);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除章节")
    public Result<Void> delete(@PathVariable Long id) {
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
