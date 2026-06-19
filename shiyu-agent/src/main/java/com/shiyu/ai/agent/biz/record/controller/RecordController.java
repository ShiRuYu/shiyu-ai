package com.shiyu.ai.agent.biz.record.controller;

import com.shiyu.ai.agent.biz.record.service.RecordService;
import com.shiyu.ai.agent.domain.bo.RecordBO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "记录内容管理")
@RestController
@RequestMapping("/api/record")
public class RecordController {

    @Resource
    private RecordService recordService;

    @Operation(summary = "分页查询记录列表")
    @GetMapping("/page")
    public Result<PageData<RecordBO>> getPage(
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long eventId) {
        Pair<Long, List<RecordBO>> page = recordService.getPage(pageNo, pageSize, eventId);
        return Result.success(new PageData<>(page.getRight(), page.getLeft()));
    }

    @Operation(summary = "根据ID查询记录")
    @GetMapping("/{id}")
    public Result<RecordBO> getById(@PathVariable Long id) {
        return Result.success(recordService.getById(id));
    }

    @Operation(summary = "创建记录")
    @PostMapping
    public Result<RecordBO> create(@Valid @RequestBody RecordBO recordBO) {
        return Result.success(recordService.create(recordBO));
    }

    @Operation(summary = "更新记录")
    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody RecordBO recordBO) {
        return Result.success(recordService.update(recordBO));
    }

    @Operation(summary = "删除记录")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(recordService.delete(id));
    }
}
