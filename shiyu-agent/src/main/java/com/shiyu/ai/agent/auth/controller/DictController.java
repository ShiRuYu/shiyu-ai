package com.shiyu.ai.agent.auth.controller;

import com.shiyu.ai.agent.domain.bo.DictBO;
import com.shiyu.ai.agent.service.DictService;
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
    public ResponseEntity<Map<String, Object>> getDictList(
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize) {
        log.info("获取字典列表，pageNumber: {}, pageSize: {}", pageNumber, pageSize);
        
        // 设置默认值
        if (pageNumber == null) pageNumber = 1;
        if (pageSize == null) pageSize = 10;
        
        Pair<Long, List<DictBO>> result = dictService.getAll(pageNumber, pageSize);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "OK");
        
        Map<String, Object> data = new HashMap<>();
        data.put("total", result.getLeft());
        data.put("list", result.getRight());
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID查询字典详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDictById(@PathVariable Long id) {
        log.info("查询字典详情，id: {}", id);
        
        DictBO dictBO = dictService.getById(id);
        
        if (dictBO != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "OK");
            response.put("data", dictBO);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 1,
                    "message", "字典不存在"
            ));
        }
    }

    /**
     * 根据字典类型查询字典列表
     */
    @GetMapping("/type/{dictType}")
    public ResponseEntity<Map<String, Object>> getDictByType(@PathVariable String dictType) {
        log.info("根据字典类型查询字典列表，dictType: {}", dictType);
        
        List<DictBO> dictList = dictService.getByDictType(dictType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "OK");
        response.put("data", dictList);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 新增字典
     */
    @PostMapping("")
    public ResponseEntity<Map<String, Object>> createDict(@RequestBody DictBO dictBO) {
        log.info("新增字典");
        
        try {
            DictBO createdDict = dictService.create(dictBO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "新增成功");
            response.put("data", createdDict);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("新增字典失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 1,
                    "message", "新增失败：" + e.getMessage()
            ));
        }
    }

    /**
     * 修改字典
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateDict(
            @PathVariable Long id,
            @RequestBody DictBO dictBO) {
        log.info("修改字典，id: {}", id);
        
        try {
            dictBO.setId(id);
            DictBO updatedDict = dictService.update(dictBO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "修改成功");
            response.put("data", updatedDict);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("修改字典失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 1,
                    "message", "修改失败：" + e.getMessage()
            ));
        }
    }

    /**
     * 删除字典
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteDict(@PathVariable Long id) {
        log.info("删除字典，id: {}", id);
        
        try {
            dictService.deleteById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除字典失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 1,
                    "message", "删除失败：" + e.getMessage()
            ));
        }
    }

    /**
     * 批量删除字典
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> deleteDicts(@RequestBody List<Long> ids) {
        log.info("批量删除字典，ids: {}", ids);
        
        try {
            dictService.deleteByIds(ids);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "批量删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("批量删除字典失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 1,
                    "message", "批量删除失败：" + e.getMessage()
            ));
        }
    }
}
