package com.shiyu.ai.record.controller;

import com.shiyu.ai.record.service.RecordService;
import com.shiyu.ai.record.bo.RecordBO;
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

@Tag(name = "记录内容管理")
@RestController
@RequestMapping("/record/record")
public class RecordController {

    @Resource
    private RecordService recordService;

    @Operation(summary = "分页查询记录列表")
    @GetMapping("/list")
    public Result<PageData<RecordBO>> getPage(PageQuery pageQuery,
                                              @RequestParam(required = false) Long eventId) {
        Pair<Long, List<RecordBO>> page = recordService.getPage(pageQuery.getPageNum(), pageQuery.getPageSize(), eventId);
        return Result.success(new PageData<>(page.getRight(), page.getLeft()));
    }

    @Operation(summary = "根据ID查询记录")
    @GetMapping("/detail")
    public Result<RecordBO> getById(@RequestParam Long id) {
        return Result.success(recordService.getById(id));
    }

    @Operation(summary = "创建记录")
    @PostMapping("/create")
    public Result<RecordBO> create(@Valid @RequestBody RecordBO recordBO) {
        return Result.success(recordService.create(recordBO));
    }

    @Operation(summary = "更新记录")
    @PostMapping("/update")
    public Result<Boolean> update(@Valid @RequestBody RecordBO recordBO) {
        return Result.success(recordService.update(recordBO));
    }

    @Operation(summary = "删除记录")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long id) {
        return Result.success(recordService.delete(id));
    }
}
