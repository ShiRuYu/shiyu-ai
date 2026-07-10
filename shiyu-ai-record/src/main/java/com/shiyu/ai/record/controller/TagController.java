package com.shiyu.ai.record.controller;

import com.shiyu.ai.record.service.TagService;
import com.shiyu.ai.dal.bo.record.TagBO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.PageQuery;
import com.shiyu.ai.common.core.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "标签管理")
@RestController
@RequestMapping("/record/tag")
public class TagController {

    @Resource
    private TagService tagService;

    @Operation(summary = "分页查询标签列表")
    @GetMapping("/list")
    public Result<PageData<TagBO>> getPage(PageQuery pageQuery,
                                            @RequestParam(required = false) String name) {
        Pair<Long, List<TagBO>> page = tagService.getPage(pageQuery.getPageNum(), pageQuery.getPageSize(), name);
        return Result.success(new PageData<>(page.getRight(), page.getLeft()));
    }

    @Operation(summary = "查询所有标签")
    @GetMapping("/all")
    public Result<List<TagBO>> getAll() {
        return Result.success(tagService.getAll());
    }

    @Operation(summary = "根据ID查询标签")
    @GetMapping("/detail")
    public Result<TagBO> getById(@RequestParam Long id) {
        return Result.success(tagService.getById(id));
    }

    @Operation(summary = "创建标签")
    @PostMapping("/create")
    public Result<TagBO> create(@Valid @RequestBody TagBO tagBO) {
        return Result.success(tagService.create(tagBO));
    }

    @Operation(summary = "更新标签")
    @PostMapping("/update")
    public Result<Boolean> update(@Valid @RequestBody TagBO tagBO) {
        return Result.success(tagService.update(tagBO));
    }

    @Operation(summary = "删除标签")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long id) {
        return Result.success(tagService.delete(id));
    }
}
