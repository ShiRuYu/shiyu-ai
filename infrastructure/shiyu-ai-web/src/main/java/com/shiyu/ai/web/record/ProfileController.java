package com.shiyu.ai.web.record;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.PageQuery;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.record.request.ProfileRequest;
import com.shiyu.ai.record.service.ProfileService;
import com.shiyu.ai.record.vo.ProfileVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "档案管理")
@SaCheckPermission("record:profile:list")
@RestController
@RequestMapping("/v1/record/profile")
public class ProfileController {
    @Resource private ProfileService profileService;
    @GetMapping("/list")
    public Result<PageData<ProfileVO>> getPage(PageQuery query, @RequestParam(required = false) String createBy) {
        var page = profileService.pageView(query.getPageNum(), query.getPageSize(), createBy);
        return Result.success(new PageData<>(page.getRight(), page.getLeft()));
    }
    @GetMapping("/detail") public Result<ProfileVO> getById(@RequestParam Long id) { return Result.success(profileService.detailView(id)); }
    @SaCheckPermission("record:profile:create")
    @PostMapping("/create") public Result<ProfileVO> create(@Valid @RequestBody ProfileRequest request) { return Result.success(profileService.create(request)); }
    @SaCheckPermission("record:profile:edit")
    @PostMapping("/update") public Result<Boolean> update(@RequestParam Long id, @Valid @RequestBody ProfileRequest request) { return Result.success(profileService.update(id, request)); }
    @SaCheckPermission("record:profile:delete")
    @PostMapping("/delete") public Result<Boolean> delete(@RequestParam Long id) { return Result.success(profileService.delete(id)); }
}
