package com.shiyu.ai.record.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.record.service.ProfileService;
import com.shiyu.ai.dal.record.bo.ProfileBO;
import com.shiyu.ai.record.vo.ProfileVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.PageQuery;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.record.request.ProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "档案管理")
@SaCheckPermission("record:profile:list")
@RestController
@RequestMapping("/record/profile")
public class ProfileController {

    @Resource
    private ProfileService profileService;

    @Operation(summary = "分页查询档案列表")
    @GetMapping("/list")
    public Result<PageData<ProfileVO>> getPage(PageQuery pageQuery,
                                                @RequestParam(required = false) String createBy) {
        Pair<Long, List<ProfileBO>> page = profileService.getPage(pageQuery.getPageNum(), pageQuery.getPageSize(), createBy);
        return Result.success(new PageData<>(MapstructUtils.convert(page.getRight(), ProfileVO.class), page.getLeft()));
    }

    @Operation(summary = "根据ID查询档案")
    @GetMapping("/detail")
    public Result<ProfileVO> getById(@RequestParam Long id) {
        return Result.success(MapstructUtils.convert(profileService.getById(id), ProfileVO.class));
    }

    @Operation(summary = "创建档案")
    @SaCheckPermission("record:profile:create")
    @PostMapping("/create")
    public Result<ProfileVO> create(@Valid @RequestBody ProfileRequest request) {
        ProfileBO bo = new ProfileBO();
        bo.setName(request.getName());
        bo.setAvatar(request.getAvatar());
        return Result.success(MapstructUtils.convert(profileService.create(bo), ProfileVO.class));
    }

    @Operation(summary = "更新档案")
    @SaCheckPermission("record:profile:edit")
    @PostMapping("/update")
    public Result<Boolean> update(@RequestParam Long id, @Valid @RequestBody ProfileRequest request) {
        ProfileBO bo = profileService.getById(id);
        if (bo == null) return Result.fail("档案不存在");
        bo.setName(request.getName());
        bo.setAvatar(request.getAvatar());
        return Result.success(profileService.update(bo));
    }

    @Operation(summary = "删除档案")
    @SaCheckPermission("record:profile:delete")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long id) {
        return Result.success(profileService.delete(id));
    }
}
