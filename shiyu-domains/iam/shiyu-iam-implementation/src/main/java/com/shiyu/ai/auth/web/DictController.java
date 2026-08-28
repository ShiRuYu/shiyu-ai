package com.shiyu.ai.auth.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.auth.request.DictPageRequest;
import com.shiyu.ai.auth.request.DictRequest;
import com.shiyu.ai.auth.vo.DictVO;
import com.shiyu.ai.auth.service.DictService;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;

import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

/**
 * 字典管理 Controller
 *
 * 注意：所有参数均通过 @RequestParam 或 @RequestBody 传入，不使用 @PathVariable。
 */
@Slf4j
@Tag(name = "Dict", description = "Dict")
@RestController
@RequestMapping("/api/iam/dicts")
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    @Operation(summary = "Get Dict List")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/list")
    public Result<PageData<DictVO>> getDictList(@Valid DictPageRequest request) {
        log.info("获取字典列表，pageNum: {}, pageSize: {}", request.getPageNum(), request.getPageSize());
        var result = dictService.pageView(ActorContextHttpAdapter.currentActor(), request.getPageNum(), request.getPageSize());
        return Result.success(new PageData<>(result.getRight(), result.getLeft()));
    }

    @Operation(summary = "Get Dict By Type")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/type")
    public Result<List<DictVO>> getDictByType(@RequestParam String dictType) {
        log.info("根据字典类型查询字典列表，dictType: {}", dictType);
        return Result.success(dictService.byTypeView(ActorContextHttpAdapter.currentActor(), dictType));
    }

    @Operation(summary = "Create Dict")
    @SaCheckPermission("system:dict:create")
    @PostMapping("/create")
    public Result<DictVO> createDict(@Valid @RequestBody DictRequest dictBO) {
        log.info("新增字典");
        return Result.success(dictService.create(ActorContextHttpAdapter.currentActor(), dictBO));
    }

    @Operation(summary = "Update Dict")
    @SaCheckPermission("system:dict:update")
    @PostMapping("/update")
    public Result<DictVO> updateDict(@RequestParam Long id, @Valid @RequestBody DictRequest dictBO) {
        log.info("修改字典，id: {}", id);
        return Result.success(dictService.update(ActorContextHttpAdapter.currentActor(), id, dictBO));
    }

    @Operation(summary = "Delete Dict")
    @SaCheckPermission("system:dict:delete")
    @PostMapping("/delete")
    public Result<Void> deleteDict(@RequestParam Long id) {
        log.info("删除字典，id: {}", id);
        dictService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }

    @Operation(summary = "Delete Dicts")
    @SaCheckPermission("system:dict:delete")
    @PostMapping("/batch-delete")
    public Result<Void> deleteDicts(@RequestBody List<Long> ids) {
        log.info("批量删除字典，ids: {}", ids);
        dictService.deleteByIds(ActorContextHttpAdapter.currentActor(), ids);
        return Result.success();
    }
}
