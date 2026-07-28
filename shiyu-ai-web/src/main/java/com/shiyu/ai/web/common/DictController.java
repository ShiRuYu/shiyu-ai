package com.shiyu.ai.web.common;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.auth.request.DictPageRequest;
import com.shiyu.ai.dal.common.bo.DictBO;
import com.shiyu.ai.auth.vo.DictVO;
import com.shiyu.ai.auth.service.DictService;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
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
@RequestMapping("/dict")
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    @Operation(summary = "Get Dict List")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/list")
    public Result<PageData<DictVO>> getDictList(@Valid DictPageRequest request) {
        log.info("获取字典列表，pageNo: {}, pageSize: {}", request.getPageNo(), request.getPageSize());
        Pair<Long, List<DictBO>> result = dictService.getAll(request.getPageNo(), request.getPageSize());
        java.util.List<DictVO> dictVOs = com.shiyu.ai.auth.service.convert.DictConverter.INSTANCE.toVOList(result.getRight());
        return Result.success(new PageData<>(dictVOs, result.getLeft()));
    }

    @Operation(summary = "Get Dict By Type")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/type")
    public Result<List<DictVO>> getDictByType(@RequestParam String dictType) {
        log.info("根据字典类型查询字典列表，dictType: {}", dictType);
        List<DictBO> dictList = dictService.getByDictType(dictType);
        return Result.success(com.shiyu.ai.auth.service.convert.DictConverter.INSTANCE.toVOList(dictList));
    }

    @Operation(summary = "Create Dict")
    @SaCheckPermission("system:dict:create")
    @PostMapping("/create")
    public Result<DictVO> createDict(@Valid @RequestBody DictBO dictBO) {
        log.info("新增字典");
        try {
            DictBO created = dictService.create(dictBO);
            return Result.success(com.shiyu.ai.auth.service.convert.DictConverter.INSTANCE.toVO(created));
        } catch (Exception e) {
            log.error("新增字典失败", e);
            return Result.fail("新增失败");
        }
    }

    @Operation(summary = "Update Dict")
    @SaCheckPermission("system:dict:update")
    @PostMapping("/update")
    public Result<DictVO> updateDict(@RequestParam Long id, @Valid @RequestBody DictBO dictBO) {
        log.info("修改字典，id: {}", id);
        try {
            dictBO.setId(id);
            DictBO updated = dictService.update(dictBO);
            return Result.success(com.shiyu.ai.auth.service.convert.DictConverter.INSTANCE.toVO(updated));
        } catch (Exception e) {
            log.error("修改字典失败", e);
            return Result.fail("修改失败");
        }
    }

    @Operation(summary = "Delete Dict")
    @SaCheckPermission("system:dict:delete")
    @PostMapping("/delete")
    public Result<Void> deleteDict(@RequestParam Long id) {
        log.info("删除字典，id: {}", id);
        try {
            dictService.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除字典失败", e);
            return Result.fail("删除失败");
        }
    }

    @Operation(summary = "Delete Dicts")
    @SaCheckPermission("system:dict:delete")
    @PostMapping("/batch-delete")
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
