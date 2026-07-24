package com.shiyu.ai.record.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.record.service.TagService;
import com.shiyu.ai.dal.record.bo.TagBO;
import com.shiyu.ai.record.vo.TagVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.PageQuery;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.record.request.TagRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "标签管理")
@SaCheckPermission("record:tags:list")
@RestController
@RequestMapping("/record/tag")
public class TagController {

    @Resource
    private TagService tagService;

    @Operation(summary = "分页查询标签列表")
    @GetMapping("/list")
    public Result<PageData<TagVO>> getPage(PageQuery pageQuery,
                                            @RequestParam(required = false) String name) {
        Pair<Long, List<TagBO>> page = tagService.getPage(pageQuery.getPageNum(), pageQuery.getPageSize(), name);
        return Result.success(new PageData<>(MapstructUtils.convert(page.getRight(), TagVO.class), page.getLeft()));
    }

    @Operation(summary = "查询所有标签")
    @GetMapping("/all")
    public Result<List<TagVO>> getAll() {
        return Result.success(MapstructUtils.convert(tagService.getAll(), TagVO.class));
    }

    @Operation(summary = "根据ID查询标签")
    @GetMapping("/detail")
    public Result<TagVO> getById(@RequestParam Long id) {
        return Result.success(MapstructUtils.convert(tagService.getById(id), TagVO.class));
    }

    @Operation(summary = "创建标签")
    @PostMapping("/create")
    public Result<TagVO> create(@Valid @RequestBody TagRequest request) {
        TagBO bo = new TagBO();
        bo.setName(request.getName());
        return Result.success(MapstructUtils.convert(tagService.create(bo), TagVO.class));
    }

    @Operation(summary = "更新标签")
    @PostMapping("/update")
    public Result<Boolean> update(@RequestParam Long id, @Valid @RequestBody TagRequest request) {
        TagBO bo = tagService.getById(id);
        if (bo == null) return Result.fail("标签不存在");
        bo.setName(request.getName());
        return Result.success(tagService.update(bo));
    }

    @Operation(summary = "删除标签")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long id) {
        return Result.success(tagService.delete(id));
    }
}
