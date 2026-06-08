package com.shiyu.ai.agent.biz.record.controller;

import com.shiyu.ai.agent.domain.bo.ProfileBO;
import com.shiyu.ai.agent.biz.record.service.ProfileService;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 人物管理控制器
 */
@Tag(name = "人物管理", description = "个人成长记录系统 - 人物管理")
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Resource
    private ProfileService profileService;

    /**
     * 分页查询人物列表
     */
    @Operation(summary = "分页查询人物列表")
    @GetMapping("/page")
    public Result<PageData<ProfileBO>> getPage(
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String createBy) {
        Pair<Long, List<ProfileBO>> page = profileService.getPage(pageNo, pageSize, createBy);
        PageData<ProfileBO> pageData = new PageData<>(page.getRight(), page.getLeft());
        return Result.success(pageData);
    }

    /**
     * 根据ID查询人物
     */
    @Operation(summary = "根据ID查询人物")
    @GetMapping("/{id}")
    public Result<ProfileBO> getById(@PathVariable Long id) {
        ProfileBO profile = profileService.getById(id);
        return Result.success(profile);
    }

    /**
     * 创建人物
     */
    @Operation(summary = "创建人物")
    @PostMapping
    public Result<ProfileBO> create(@RequestBody ProfileBO profileBO) {
        ProfileBO created = profileService.create(profileBO);
        return Result.success(created);
    }

    /**
     * 更新人物
     */
    @Operation(summary = "更新人物")
    @PutMapping
    public Result<Boolean> update(@RequestBody ProfileBO profileBO) {
        boolean updated = profileService.update(profileBO);
        return Result.success(updated);
    }

    /**
     * 删除人物
     */
    @Operation(summary = "删除人物")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean deleted = profileService.delete(id);
        return Result.success(deleted);
    }
}
