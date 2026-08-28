package com.shiyu.ai.record.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.PageQuery;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.record.request.MediaRequest;
import com.shiyu.ai.record.service.MediaService;
import com.shiyu.ai.record.vo.MediaVO;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "附件管理")
@SaCheckPermission("record:media:list")
@RestController
@RequestMapping("/api/record/media")
public class MediaController {
    @Resource private MediaService mediaService;
    @Operation(summary = "分页查询附件列表")
    @GetMapping("/list")
    public Result<PageData<MediaVO>> getPage(PageQuery query, @RequestParam(required = false) Long recordId) {
        var page = mediaService.pageView(ActorContextHttpAdapter.currentActor(), query.getPageNum(), query.getPageSize(), recordId);
        return Result.success(new PageData<>(page.getRight(), page.getLeft()));
    }
    @Operation(summary = "查询附件")
    @GetMapping("/detail")
    public Result<MediaVO> getById(@RequestParam Long id) { return Result.success(mediaService.detailView(ActorContextHttpAdapter.currentActor(), id)); }
    @SaCheckPermission("record:media:upload")
    @PostMapping("/create")
    public Result<MediaVO> create(@Valid @RequestBody MediaRequest request) { return Result.success(mediaService.create(ActorContextHttpAdapter.currentActor(), request)); }
    @SaCheckPermission("record:media:upload")
    @PostMapping("/update")
    public Result<Boolean> update(@RequestParam Long id, @Valid @RequestBody MediaRequest request) { return Result.success(mediaService.update(ActorContextHttpAdapter.currentActor(), id, request)); }
    @SaCheckPermission("record:media:upload")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long id) { return Result.success(mediaService.delete(ActorContextHttpAdapter.currentActor(), id)); }
}
