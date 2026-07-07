package com.shiyu.ai.record.controller;

import com.shiyu.ai.record.bo.TimelineEventBO;
import com.shiyu.ai.record.service.TimelineEventService;
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
 * 时间轴事件控制器
 */
@Tag(name = "时间轴管理", description = "个人成长记录系统 - 时间轴事件管理")
@RestController
@RequestMapping("/record/timeline")
public class TimelineEventController {

    @Resource
    private TimelineEventService timelineEventService;

    /**
     * 分页查询时间轴事件列表
     */
    @Operation(summary = "分页查询时间轴事件列表")
    @GetMapping("/list")
    public Result<PageData<TimelineEventBO>> getPage(PageQuery pageQuery,
                                                      @RequestParam Long profileId) {
        Pair<Long, List<TimelineEventBO>> page = timelineEventService.getPage(pageQuery.getPageNum(), pageQuery.getPageSize(), profileId);
        PageData<TimelineEventBO> pageData = new PageData<>(page.getRight(), page.getLeft());
        return Result.success(pageData);
    }

    /**
     * 根据ID查询时间轴事件
     */
    @Operation(summary = "根据ID查询时间轴事件")
    @GetMapping("/detail")
    public Result<TimelineEventBO> getById(@RequestParam Long id) {
        TimelineEventBO event = timelineEventService.getById(id);
        return Result.success(event);
    }

    /**
     * 创建时间轴事件
     */
    @Operation(summary = "创建时间轴事件")
    @PostMapping("/create")
    public Result<TimelineEventBO> create(@Valid @RequestBody TimelineEventBO eventBO) {
        TimelineEventBO created = timelineEventService.create(eventBO);
        return Result.success(created);
    }

    /**
     * 更新时间轴事件
     */
    @Operation(summary = "更新时间轴事件")
    @PostMapping("/update")
    public Result<Boolean> update(@Valid @RequestBody TimelineEventBO eventBO) {
        boolean updated = timelineEventService.update(eventBO);
        return Result.success(updated);
    }

    /**
     * 删除时间轴事件
     */
    @Operation(summary = "删除时间轴事件")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long id) {
        boolean deleted = timelineEventService.delete(id);
        return Result.success(deleted);
    }

    /**
     * 查询人物的完整时间轴
     */
    @Operation(summary = "查询人物的完整时间轴")
    @GetMapping("/profile")
    public Result<List<TimelineEventBO>> getTimelineByProfileId(@RequestParam Long profileId) {
        List<TimelineEventBO> timeline = timelineEventService.getTimelineByProfileId(profileId);
        return Result.success(timeline);
    }
}
