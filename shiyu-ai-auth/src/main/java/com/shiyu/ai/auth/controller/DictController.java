package com.shiyu.ai.auth.controller;

import com.shiyu.ai.auth.request.DictPageRequest;
import com.shiyu.ai.dal.bo.auth.DictBO;
import com.shiyu.ai.auth.vo.DictVO;
import com.shiyu.ai.auth.service.DictService;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

/**
 * 字典管理 Controller
 */
@Slf4j
@Tag(name = "Dict", description = "Dict")
@RestController
@RequestMapping("/dict")
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    /**
     * 字典列表 - 分页
     */
    @Operation(summary = "Get Dict List")
    @GetMapping("/page")
    public Result<PageData<DictVO>> getDictList(@Valid DictPageRequest request) {
        log.info("获取字典列表，pageNo: {}, pageSize: {}", request.getPageNo(), request.getPageSize());
        Pair<Long, List<DictBO>> result = dictService.getAll(request.getPageNo(), request.getPageSize());
        List<DictVO> dictVOs = MapstructUtils.convert(result.getRight(), DictVO.class);
        return Result.success(new PageData<>(dictVOs, result.getLeft()));
    }

    /**
     * 根据ID查询字典详情
     */
    @Operation(summary = "Get Dict By Id")
    @GetMapping("/{id}")
    public Result<DictVO> getDictById(@PathVariable Long id) {
        log.info("查询字典详情，id: {}", id);
        DictBO dictBO = dictService.getById(id);
        if (dictBO == null) return Result.fail("字典不存在");
        return Result.success(MapstructUtils.convert(dictBO, DictVO.class));
    }

    /**
     * 根据字典类型查询字典列表
     */
    @Operation(summary = "Get Dict By Type")
    @GetMapping("/type/{dictType}")
    public Result<List<DictVO>> getDictByType(@PathVariable String dictType) {
        log.info("根据字典类型查询字典列表，dictType: {}", dictType);
        List<DictBO> dictList = dictService.getByDictType(dictType);
        return Result.success(MapstructUtils.convert(dictList, DictVO.class));
    }

    /**
     * 新增字典
     */
    @Operation(summary = "Create Dict")
    @PostMapping("")
    public Result<DictVO> createDict(@Valid @RequestBody DictBO dictBO) {
        log.info("新增字典");
        try {
            DictBO created = dictService.create(dictBO);
            return Result.success(MapstructUtils.convert(created, DictVO.class));
        } catch (Exception e) {
            log.error("新增字典失败", e);
            return Result.fail("新增失败");
        }
    }

    /**
     * 修改字典
     */
    @Operation(summary = "Update Dict")
    @PatchMapping("/{id}")
    public Result<DictVO> updateDict(@PathVariable Long id, @Valid @RequestBody DictBO dictBO) {
        log.info("修改字典，id: {}", id);
        try {
            dictBO.setId(id);
            DictBO updated = dictService.update(dictBO);
            return Result.success(MapstructUtils.convert(updated, DictVO.class));
        } catch (Exception e) {
            log.error("修改字典失败", e);
            return Result.fail("修改失败");
        }
    }

    /**
     * 删除字典
     */
    @Operation(summary = "Delete Dict")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDict(@PathVariable Long id) {
        log.info("删除字典，id: {}", id);
        try {
            dictService.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除字典失败", e);
            return Result.fail("删除失败");
        }
    }

    /**
     * 批量删除字典
     */
    @Operation(summary = "Delete Dicts")
    @DeleteMapping("/batch")
    public Result<Void> deleteDicts(@RequestBody List<Long> ids) {
        log.info("批量删除字典，ids: {}", ids);
        try {
            dictService.deleteByIds(ids);
            return Result.success();
        } catch (Exception e) {
            log.error("批量删除字典失败", e);
            return Result.fail("批量删除失败");
        }
    }
}
