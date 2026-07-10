package com.shiyu.ai.record.controller;

import com.shiyu.ai.record.service.MediaService;
import com.shiyu.ai.dal.bo.record.MediaBO;
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

@Tag(name = "附件管理")
@RestController
@RequestMapping("/record/media")
public class MediaController {

    @Resource
    private MediaService mediaService;

    @Operation(summary = "分页查询附件列表")
    @GetMapping("/list")
    public Result<PageData<MediaBO>> getPage(PageQuery pageQuery,
                                              @RequestParam(required = false) Long recordId) {
        Pair<Long, List<MediaBO>> page = mediaService.getPage(pageQuery.getPageNum(), pageQuery.getPageSize(), recordId);
        return Result.success(new PageData<>(page.getRight(), page.getLeft()));
    }

    @Operation(summary = "根据ID查询附件")
    @GetMapping("/detail")
    public Result<MediaBO> getById(@RequestParam Long id) {
        return Result.success(mediaService.getById(id));
    }

    @Operation(summary = "创建附件")
    @PostMapping("/create")
    public Result<MediaBO> create(@Valid @RequestBody MediaBO mediaBO) {
        return Result.success(mediaService.create(mediaBO));
    }

    @Operation(summary = "更新附件")
    @PostMapping("/update")
    public Result<Boolean> update(@Valid @RequestBody MediaBO mediaBO) {
        return Result.success(mediaService.update(mediaBO));
    }

    @Operation(summary = "删除附件")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long id) {
        return Result.success(mediaService.delete(id));
    }
}
