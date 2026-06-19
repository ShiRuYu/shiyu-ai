package com.shiyu.ai.agent.biz.common.controller;

import com.shiyu.ai.agent.biz.common.service.DictService;
import com.shiyu.ai.agent.domain.bo.DictBO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理 Controller
 */
@Slf4j
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
    @GetMapping("/page")
    public Result<PageData<DictBO>> getDictList(
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        log.info("获取字典列表，pageNo: {}, pageSize: {}", pageNo, pageSize);
        
        Pair<Long, List<DictBO>> result = dictService.getAll(pageNo, pageSize);
        PageData<DictBO> pageData = new PageData<>(result.getRight(), result.getLeft());
        return Result.success(pageData);
    }

    /**
     * 根据ID查询字典详情
     */
    @GetMapping("/{id}")
    public Result<DictBO> getDictById(@PathVariable Long id) {
        log.info("查询字典详情，id: {}", id);
        
        DictBO dictBO = dictService.getById(id);
        
        if (dictBO != null) {
            return Result.success(dictBO);
        } else {
            return Result.fail("字典不存在");
        }
    }

    /**
     * 根据字典类型查询字典列表
     */
    @GetMapping("/type/{dictType}")
    public Result<List<DictBO>> getDictByType(@PathVariable String dictType) {
        log.info("根据字典类型查询字典列表，dictType: {}", dictType);
        
        List<DictBO> dictList = dictService.getByDictType(dictType);
        
        return Result.success(dictList);
    }

    /**
     * 新增字典
     */
    @PostMapping("")
    public Result<DictBO> createDict(@Valid @RequestBody DictBO dictBO) {
        log.info("新增字典");
        
        try {
            DictBO createdDict = dictService.create(dictBO);
            return Result.success(createdDict);
        } catch (Exception e) {
            log.error("新增字典失败", e);
            return Result.fail("新增失败");
        }
    }

    /**
     * 修改字典
     */
    @PatchMapping("/{id}")
    public Result<DictBO> updateDict(
            @PathVariable Long id,
            @Valid @RequestBody DictBO dictBO) {
        log.info("修改字典，id: {}", id);
        
        try {
            dictBO.setId(id);
            DictBO updatedDict = dictService.update(dictBO);
            return Result.success(updatedDict);
        } catch (Exception e) {
            log.error("修改字典失败", e);
            return Result.fail("修改失败");
        }
    }

    /**
     * 删除字典
     */
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
