package com.shiyu.ai.record.controller;

import com.shiyu.ai.record.bo.ProfileBO;
import com.shiyu.ai.record.service.ProfileService;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 浜虹墿绠＄悊鎺у埗鍣?
 */
@Tag(name = "浜虹墿绠＄悊", description = "涓汉鎴愰暱璁板綍绯荤粺 - 浜虹墿绠＄悊")
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Resource
    private ProfileService profileService;

    /**
     * 鍒嗛〉鏌ヨ浜虹墿鍒楄〃
     */
    @Operation(summary = "鍒嗛〉鏌ヨ浜虹墿鍒楄〃")
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
     * 鏍规嵁ID鏌ヨ浜虹墿
     */
    @Operation(summary = "鏍规嵁ID鏌ヨ浜虹墿")
    @GetMapping("/{id}")
    public Result<ProfileBO> getById(@PathVariable Long id) {
        ProfileBO profile = profileService.getById(id);
        return Result.success(profile);
    }

    /**
     * 鍒涘缓浜虹墿
     */
    @Operation(summary = "鍒涘缓浜虹墿")
    @PostMapping
    public Result<ProfileBO> create(@Valid @RequestBody ProfileBO profileBO) {
        ProfileBO created = profileService.create(profileBO);
        return Result.success(created);
    }

    /**
     * 鏇存柊浜虹墿
     */
    @Operation(summary = "鏇存柊浜虹墿")
    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody ProfileBO profileBO) {
        boolean updated = profileService.update(profileBO);
        return Result.success(updated);
    }

    /**
     * 鍒犻櫎浜虹墿
     */
    @Operation(summary = "鍒犻櫎浜虹墿")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean deleted = profileService.delete(id);
        return Result.success(deleted);
    }
}
