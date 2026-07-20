package com.shiyu.ai.record.controller;

import com.shiyu.ai.record.service.TimelineEventService;
import com.shiyu.ai.dal.record.bo.TimelineEventBO;
import com.shiyu.ai.record.vo.TimelineEventVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.PageQuery;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.record.request.TimelineEventRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "时间线事件管理")
@RestController
@RequestMapping("/record/timeline")
public class TimelineEventController {

    @Resource
    private TimelineEventService timelineEventService;

    @Operation(summary = "分页查询时间线事件列表")
    @GetMapping("/list")
    public Result<PageData<TimelineEventVO>> getPage(PageQuery pageQuery,
                                                      @RequestParam Long profileId) {
        Pair<Long, List<TimelineEventBO>> page = timelineEventService.getPage(pageQuery.getPageNum(), pageQuery.getPageSize(), profileId);
        return Result.success(new PageData<>(MapstructUtils.convert(page.getRight(), TimelineEventVO.class), page.getLeft()));
    }

    @Operation(summary = "根据ID查询时间线事件")
    @GetMapping("/detail")
    public Result<TimelineEventVO> getById(@RequestParam Long id) {
        return Result.success(MapstructUtils.convert(timelineEventService.getById(id), TimelineEventVO.class));
    }

    @Operation(summary = "创建时间线事件")
    @PostMapping("/create")
    public Result<TimelineEventVO> create(@Valid @RequestBody TimelineEventRequest request) {
        TimelineEventBO bo = new TimelineEventBO();
        bo.setProfileId(request.getProfileId());
        bo.setTitle(request.getTitle());
        if (request.getEventDate() != null) {
            bo.setEventTime(new java.util.Date(request.getEventDate().getTime()));
        }
        bo.setType(request.getEventType());
        return Result.success(MapstructUtils.convert(timelineEventService.create(bo), TimelineEventVO.class));
    }

    @Operation(summary = "更新时间线事件")
    @PostMapping("/update")
    public Result<Boolean> update(@RequestParam Long id, @Valid @RequestBody TimelineEventRequest request) {
        TimelineEventBO bo = timelineEventService.getById(id);
        if (bo == null) return Result.fail("时间线事件不存在");
        bo.setTitle(request.getTitle());
        if (request.getEventDate() != null) {
            bo.setEventTime(new java.util.Date(request.getEventDate().getTime()));
        }
        bo.setType(request.getEventType());
        return Result.success(timelineEventService.update(bo));
    }

    @Operation(summary = "删除时间线事件")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long id) {
        return Result.success(timelineEventService.delete(id));
    }

    @Operation(summary = "根据档案ID查询时间线")
    @GetMapping("/profile")
    public Result<List<TimelineEventVO>> getTimelineByProfileId(@RequestParam Long profileId) {
        return Result.success(MapstructUtils.convert(
            timelineEventService.getTimelineByProfileId(profileId), TimelineEventVO.class));
    }
}
