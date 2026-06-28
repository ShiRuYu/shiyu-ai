package com.shiyu.ai.record.controller;

import com.shiyu.ai.record.service.TagService;
import com.shiyu.ai.model.bo.TagBO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "鏍囩绠＄悊")
@RestController
@RequestMapping("/api/tag")
public class TagController {

    @Resource
    private TagService tagService;

    @Operation(summary = "鍒嗛〉鏌ヨ鏍囩鍒楄〃")
    @GetMapping("/page")
    public Result<PageData<TagBO>> getPage(
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        Pair<Long, List<TagBO>> page = tagService.getPage(pageNo, pageSize, name);
        return Result.success(new PageData<>(page.getRight(), page.getLeft()));
    }

    @Operation(summary = "鏌ヨ鎵€鏈夋爣绛?)
    @GetMapping("/all")
    public Result<List<TagBO>> getAll() {
        return Result.success(tagService.getAll());
    }

    @Operation(summary = "鏍规嵁ID鏌ヨ鏍囩")
    @GetMapping("/{id}")
    public Result<TagBO> getById(@PathVariable Long id) {
        return Result.success(tagService.getById(id));
    }

    @Operation(summary = "鍒涘缓鏍囩")
    @PostMapping
    public Result<TagBO> create(@Valid @RequestBody TagBO tagBO) {
        return Result.success(tagService.create(tagBO));
    }

    @Operation(summary = "鏇存柊鏍囩")
    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody TagBO tagBO) {
        return Result.success(tagService.update(tagBO));
    }

    @Operation(summary = "鍒犻櫎鏍囩")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(tagService.delete(id));
    }
}
