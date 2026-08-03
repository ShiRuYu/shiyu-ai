package com.shiyu.ai.web.record;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.PageQuery;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.record.request.TagRequest;
import com.shiyu.ai.record.service.TagService;
import com.shiyu.ai.record.vo.TagVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "标签管理")
@SaCheckPermission("record:tags:list")
@RestController
@RequestMapping("/record/tag")
public class TagController {
    @Resource private TagService tagService;
    @GetMapping("/list") public Result<PageData<TagVO>> getPage(PageQuery query, @RequestParam(required = false) String name) {
        var page = tagService.pageView(query.getPageNum(), query.getPageSize(), name);
        return Result.success(new PageData<>(page.getRight(), page.getLeft()));
    }
    @GetMapping("/all") public Result<java.util.List<TagVO>> getAll() { return Result.success(tagService.allView()); }
    @GetMapping("/detail") public Result<TagVO> getById(@RequestParam Long id) { return Result.success(tagService.detailView(id)); }
    @PostMapping("/create") public Result<TagVO> create(@Valid @RequestBody TagRequest request) { return Result.success(tagService.create(request)); }
    @PostMapping("/update") public Result<Boolean> update(@RequestParam Long id, @Valid @RequestBody TagRequest request) { return Result.success(tagService.update(id, request)); }
    @PostMapping("/delete") public Result<Boolean> delete(@RequestParam Long id) { return Result.success(tagService.delete(id)); }
}
