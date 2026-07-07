package com.shiyu.ai.record.controller;

import com.shiyu.ai.record.bo.ProfileBO;
import com.shiyu.ai.record.service.ProfileService;
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

/**
 * 人物管理控制器
 */
@Tag(name = "人物管理", description = "个人成长记录系统 - 人物管理")
@RestController
@RequestMapping("/record/profile")
public class ProfileController {

    @Resource
    private ProfileService profileService;

    /**
     * 分页查询人物列表
     */
    @Operation(summary = "分页查询人物列表")
    @GetMapping("/list")
    public Result<PageData<ProfileBO>> getPage(PageQuery pageQuery,
                                                @RequestParam(required = false) String createBy) {
        Pair<Long, List<ProfileBO>> page = profileService.getPage(pageQuery.getPageNum(), pageQuery.getPageSize(), createBy);
        PageData<ProfileBO> pageData = new PageData<>(page.getRight(), page.getLeft());
        return Result.success(pageData);
    }

    /**
     * 根据ID查询人物
     */
    @Operation(summary = "根据ID查询人物")
    @GetMapping("/detail")
    public Result<ProfileBO> getById(@RequestParam Long id) {
        ProfileBO profile = profileService.getById(id);
        return Result.success(profile);
    }

    /**
     * 创建人物
     */
    @Operation(summary = "创建人物")
    @PostMapping("/create")
    public Result<ProfileBO> create(@Valid @RequestBody ProfileBO profileBO) {
        ProfileBO created = profileService.create(profileBO);
        return Result.success(created);
    }

    /**
     * 更新人物
     */
    @Operation(summary = "更新人物")
    @PostMapping("/update")
    public Result<Boolean> update(@Valid @RequestBody ProfileBO profileBO) {
        boolean updated = profileService.update(profileBO);
        return Result.success(updated);
    }

    /**
     * 删除人物
     */
    @Operation(summary = "删除人物")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long id) {
        boolean deleted = profileService.delete(id);
        return Result.success(deleted);
    }
}
