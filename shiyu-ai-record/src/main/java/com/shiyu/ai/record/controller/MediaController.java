package com.shiyu.ai.record.controller;

import com.shiyu.ai.record.service.MediaService;
import com.shiyu.ai.record.bo.MediaBO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "闄勪欢绠＄悊")
@RestController
@RequestMapping("/api/media")
public class MediaController {

    @Resource
    private MediaService mediaService;

    @Operation(summary = "鍒嗛〉鏌ヨ闄勪欢鍒楄〃")
    @GetMapping("/page")
    public Result<PageData<MediaBO>> getPage(
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long recordId) {
        Pair<Long, List<MediaBO>> page = mediaService.getPage(pageNo, pageSize, recordId);
        return Result.success(new PageData<>(page.getRight(), page.getLeft()));
    }

    @Operation(summary = "鏍规嵁ID鏌ヨ闄勪欢")
    @GetMapping("/{id}")
    public Result<MediaBO> getById(@PathVariable Long id) {
        return Result.success(mediaService.getById(id));
    }

    @Operation(summary = "鍒涘缓闄勪欢")
    @PostMapping
    public Result<MediaBO> create(@Valid @RequestBody MediaBO mediaBO) {
        return Result.success(mediaService.create(mediaBO));
    }

    @Operation(summary = "鏇存柊闄勪欢")
    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody MediaBO mediaBO) {
        return Result.success(mediaService.update(mediaBO));
    }

    @Operation(summary = "鍒犻櫎闄勪欢")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(mediaService.delete(id));
    }
}
