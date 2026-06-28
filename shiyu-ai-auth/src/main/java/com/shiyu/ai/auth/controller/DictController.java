package com.shiyu.ai.auth.controller;

import com.shiyu.ai.auth.service.DictService;
import com.shiyu.ai.model.bo.DictBO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 瀛楀吀绠＄悊 Controller
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
     * 瀛楀吀鍒楄〃 - 鍒嗛〉
     */
    @GetMapping("/page")
    public Result<PageData<DictBO>> getDictList(
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        log.info("鑾峰彇瀛楀吀鍒楄〃锛宲ageNo: {}, pageSize: {}", pageNo, pageSize);
        
        Pair<Long, List<DictBO>> result = dictService.getAll(pageNo, pageSize);
        PageData<DictBO> pageData = new PageData<>(result.getRight(), result.getLeft());
        return Result.success(pageData);
    }

    /**
     * 鏍规嵁ID鏌ヨ瀛楀吀璇︽儏
     */
    @GetMapping("/{id}")
    public Result<DictBO> getDictById(@PathVariable Long id) {
        log.info("鏌ヨ瀛楀吀璇︽儏锛宨d: {}", id);
        
        DictBO dictBO = dictService.getById(id);
        
        if (dictBO != null) {
            return Result.success(dictBO);
        } else {
            return Result.fail("瀛楀吀涓嶅瓨鍦?);
        }
    }

    /**
     * 鏍规嵁瀛楀吀绫诲瀷鏌ヨ瀛楀吀鍒楄〃
     */
    @GetMapping("/type/{dictType}")
    public Result<List<DictBO>> getDictByType(@PathVariable String dictType) {
        log.info("鏍规嵁瀛楀吀绫诲瀷鏌ヨ瀛楀吀鍒楄〃锛宒ictType: {}", dictType);
        
        List<DictBO> dictList = dictService.getByDictType(dictType);
        
        return Result.success(dictList);
    }

    /**
     * 鏂板瀛楀吀
     */
    @PostMapping("")
    public Result<DictBO> createDict(@Valid @RequestBody DictBO dictBO) {
        log.info("鏂板瀛楀吀");
        
        try {
            DictBO createdDict = dictService.create(dictBO);
            return Result.success(createdDict);
        } catch (Exception e) {
            log.error("鏂板瀛楀吀澶辫触", e);
            return Result.fail("鏂板澶辫触");
        }
    }

    /**
     * 淇敼瀛楀吀
     */
    @PatchMapping("/{id}")
    public Result<DictBO> updateDict(
            @PathVariable Long id,
            @Valid @RequestBody DictBO dictBO) {
        log.info("淇敼瀛楀吀锛宨d: {}", id);
        
        try {
            dictBO.setId(id);
            DictBO updatedDict = dictService.update(dictBO);
            return Result.success(updatedDict);
        } catch (Exception e) {
            log.error("淇敼瀛楀吀澶辫触", e);
            return Result.fail("淇敼澶辫触");
        }
    }

    /**
     * 鍒犻櫎瀛楀吀
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteDict(@PathVariable Long id) {
        log.info("鍒犻櫎瀛楀吀锛宨d: {}", id);
        
        try {
            dictService.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            log.error("鍒犻櫎瀛楀吀澶辫触", e);
            return Result.fail("鍒犻櫎澶辫触");
        }
    }

    /**
     * 鎵归噺鍒犻櫎瀛楀吀
     */
    @DeleteMapping("/batch")
    public Result<Void> deleteDicts(@RequestBody List<Long> ids) {
        log.info("鎵归噺鍒犻櫎瀛楀吀锛宨ds: {}", ids);
        
        try {
            dictService.deleteByIds(ids);
            return Result.success();
        } catch (Exception e) {
            log.error("鎵归噺鍒犻櫎瀛楀吀澶辫触", e);
            return Result.fail("鎵归噺鍒犻櫎澶辫触");
        }
    }
}
