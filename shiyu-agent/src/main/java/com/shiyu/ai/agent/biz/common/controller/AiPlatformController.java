package com.shiyu.ai.agent.biz.common.controller;

import com.shiyu.ai.agent.biz.common.service.AiPlatformService;
import com.shiyu.ai.agent.domain.bo.AiPlatformBO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 平台管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/ai/platform")
public class AiPlatformController {

    private final AiPlatformService aiPlatformService;

    public AiPlatformController(AiPlatformService aiPlatformService) {
        this.aiPlatformService = aiPlatformService;
    }

    /**
     * 平台列表 - 分页
     */
    @GetMapping("/page")
    public Result<PageData<AiPlatformBO>> getPage(
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        log.info("获取平台列表，pageNo: {}, pageSize: {}", pageNo, pageSize);
        Pair<Long, List<AiPlatformBO>> result = aiPlatformService.getPage(pageNo, pageSize);
        PageData<AiPlatformBO> pageData = new PageData<>(result.getRight(), result.getLeft());
        return Result.success(pageData);
    }

    /**
     * 查询所有启用的平台
     */
    @GetMapping("/enabled")
    public Result<List<AiPlatformBO>> getAllEnabled() {
        log.info("查询所有启用的平台");
        List<AiPlatformBO> list = aiPlatformService.getAllEnabled();
        return Result.success(list);
    }

    /**
     * 根据 ID 查询平台详情
     */
    @GetMapping("/{id}")
    public Result<AiPlatformBO> getById(@PathVariable Long id) {
        log.info("查询平台详情，id: {}", id);
        AiPlatformBO bo = aiPlatformService.getById(id);
        if (bo != null) {
            return Result.success(bo);
        }
        return Result.fail("平台不存在");
    }

    /**
     * 根据编码查询平台
     */
    @GetMapping("/code/{code}")
    public Result<AiPlatformBO> getByCode(@PathVariable String code) {
        log.info("根据编码查询平台，code: {}", code);
        AiPlatformBO bo = aiPlatformService.getByCode(code);
        if (bo != null) {
            return Result.success(bo);
        }
        return Result.fail("平台不存在");
    }

    /**
     * 获取默认平台
     */
    @GetMapping("/default")
    public Result<AiPlatformBO> getDefault() {
        log.info("获取默认平台");
        AiPlatformBO bo = aiPlatformService.getDefault();
        if (bo != null) {
            return Result.success(bo);
        }
        return Result.fail("未配置默认平台");
    }

    /**
     * 新增平台
     */
    @PostMapping
    public Result<AiPlatformBO> create(@RequestBody AiPlatformBO bo) {
        log.info("新增平台：{}", bo.getName());
        try {
            AiPlatformBO created = aiPlatformService.create(bo);
            return Result.success(created);
        } catch (Exception e) {
            log.error("新增平台失败", e);
            return Result.fail("新增失败：" + e.getMessage());
        }
    }

    /**
     * 修改平台
     */
    @PatchMapping("/{id}")
    public Result<AiPlatformBO> update(@PathVariable Long id, @RequestBody AiPlatformBO bo) {
        log.info("修改平台，id: {}", id);
        try {
            bo.setId(id);
            AiPlatformBO updated = aiPlatformService.update(bo);
            return Result.success(updated);
        } catch (Exception e) {
            log.error("修改平台失败", e);
            return Result.fail("修改失败：" + e.getMessage());
        }
    }

    /**
     * 删除平台
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除平台，id: {}", id);
        try {
            aiPlatformService.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除平台失败", e);
            return Result.fail("删除失败：" + e.getMessage());
        }
    }

    /**
     * 设置为默认平台
     */
    @PutMapping("/{id}/default")
    public Result<AiPlatformBO> setDefault(@PathVariable Long id) {
        log.info("设置默认平台，id: {}", id);
        try {
            AiPlatformBO bo = aiPlatformService.setDefault(id);
            return Result.success(bo);
        } catch (Exception e) {
            log.error("设置默认平台失败", e);
            return Result.fail("设置失败：" + e.getMessage());
        }
    }
}
