package com.shiyu.ai.agent.auth.controller;

import com.shiyu.ai.agent.domain.bo.DictBO;
import com.shiyu.ai.agent.service.DictService;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Result<Map<String, Object>>> getDictList(
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize) {
        log.info("获取字典列表，pageNo: {}, pageSize: {}", pageNo, pageSize);
        
        // 设置默认值
        if (pageNo == null) pageNo = 1;
        if (pageSize == null) pageSize = 10;
        
        Pair<Long, List<DictBO>> result = dictService.getAll(pageNo, pageSize);
        
        Map<String, Object> data = new HashMap<>();
        data.put("total", result.getLeft());
        data.put("list", result.getRight());
        
        return ResponseEntity.ok(Result.success(data));
    }

    /**
     * 根据ID查询字典详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<DictBO>> getDictById(@PathVariable Long id) {
        log.info("查询字典详情，id: {}", id);
        
        DictBO dictBO = dictService.getById(id);
        
        if (dictBO != null) {
            return ResponseEntity.ok(Result.success(dictBO));
        } else {
            return ResponseEntity.badRequest().body(Result.fail("字典不存在"));
        }
    }

    /**
     * 根据字典类型查询字典列表
     */
    @GetMapping("/type/{dictType}")
    public ResponseEntity<Result<List<DictBO>>> getDictByType(@PathVariable String dictType) {
        log.info("根据字典类型查询字典列表，dictType: {}", dictType);
        
        List<DictBO> dictList = dictService.getByDictType(dictType);
        
        return ResponseEntity.ok(Result.success(dictList));
    }

    /**
     * 新增字典
     */
    @PostMapping("")
    public ResponseEntity<Result<DictBO>> createDict(@RequestBody DictBO dictBO) {
        log.info("新增字典");
        
        try {
            DictBO createdDict = dictService.create(dictBO);
            return ResponseEntity.ok(Result.success(createdDict));
        } catch (Exception e) {
            log.error("新增字典失败", e);
            return ResponseEntity.badRequest().body(Result.fail("新增失败：" + e.getMessage()));
        }
    }

    /**
     * 修改字典
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Result<DictBO>> updateDict(
            @PathVariable Long id,
            @RequestBody DictBO dictBO) {
        log.info("修改字典，id: {}", id);
        
        try {
            dictBO.setId(id);
            DictBO updatedDict = dictService.update(dictBO);
            return ResponseEntity.ok(Result.success(updatedDict));
        } catch (Exception e) {
            log.error("修改字典失败", e);
            return ResponseEntity.badRequest().body(Result.fail("修改失败：" + e.getMessage()));
        }
    }

    /**
     * 删除字典
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteDict(@PathVariable Long id) {
        log.info("删除字典，id: {}", id);
        
        try {
            dictService.deleteById(id);
            return ResponseEntity.ok(Result.success());
        } catch (Exception e) {
            log.error("删除字典失败", e);
            return ResponseEntity.badRequest().body(Result.fail("删除失败：" + e.getMessage()));
        }
    }

    /**
     * 批量删除字典
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Result<Void>> deleteDicts(@RequestBody List<Long> ids) {
        log.info("批量删除字典，ids: {}", ids);
        
        try {
            dictService.deleteByIds(ids);
            return ResponseEntity.ok(Result.success());
        } catch (Exception e) {
            log.error("批量删除字典失败", e);
            return ResponseEntity.badRequest().body(Result.fail("批量删除失败：" + e.getMessage()));
        }
    }
}
