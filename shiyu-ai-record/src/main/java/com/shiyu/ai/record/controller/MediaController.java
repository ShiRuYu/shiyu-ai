package com.shiyu.ai.record.controller;

import com.shiyu.ai.record.service.MediaService;
import com.shiyu.ai.dal.bo.record.MediaBO;
import com.shiyu.ai.record.vo.MediaVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.PageQuery;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.record.request.MediaRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "附件管理")
@RestController
@RequestMapping("/record/media")
public class MediaController {

    @Resource
    private MediaService mediaService;

    @Operation(summary = "分页查询附件列表")
    @GetMapping("/list")
    public Result<PageData<MediaVO>> getPage(PageQuery pageQuery,
                                              @RequestParam(required = false) Long recordId) {
        Pair<Long, List<MediaBO>> page = mediaService.getPage(pageQuery.getPageNum(), pageQuery.getPageSize(), recordId);
        return Result.success(new PageData<>(MapstructUtils.convert(page.getRight(), MediaVO.class), page.getLeft()));
    }

    @Operation(summary = "根据ID查询附件")
    @GetMapping("/detail")
    public Result<MediaVO> getById(@RequestParam Long id) {
        return Result.success(MapstructUtils.convert(mediaService.getById(id), MediaVO.class));
    }

    @Operation(summary = "创建附件")
    @PostMapping("/create")
    public Result<MediaVO> create(@Valid @RequestBody MediaRequest request) {
        MediaBO bo = new MediaBO();
        bo.setRecordId(request.getRecordId());
        bo.setUrl(request.getUrl());
        bo.setType(request.getMediaType());
        return Result.success(MapstructUtils.convert(mediaService.create(bo), MediaVO.class));
    }

    @Operation(summary = "更新附件")
    @PostMapping("/update")
    public Result<Boolean> update(@RequestParam Long id, @Valid @RequestBody MediaRequest request) {
        MediaBO bo = mediaService.getById(id);
        if (bo == null) return Result.fail("附件不存在");
        bo.setUrl(request.getUrl());
        bo.setType(request.getMediaType());
        return Result.success(mediaService.update(bo));
    }

    @Operation(summary = "删除附件")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long id) {
        return Result.success(mediaService.delete(id));
    }
}
