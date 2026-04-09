package com.shiyu.ai.agent.biz.record.controller;

import com.shiyu.ai.agent.domain.bo.TimelineEventBO;
import com.shiyu.ai.agent.biz.record.service.TimelineEventService;
import com.shiyu.ai.common.core.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 时间轴事件控制器
 */
@Tag(name = "时间轴管理", description = "个人成长记录系统 - 时间轴事件管理")
@RestController
@RequestMapping("/api/timeline")
public class TimelineEventController {

    @Resource
    private TimelineEventService timelineEventService;

    /**
     * 分页查询时间轴事件列表
     */
    @Operation(summary = "分页查询时间轴事件列表")
    @GetMapping("/page")
    public Result<Pair<Long, List<TimelineEventBO>>> getPage(
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam Long profileId) {
        Pair<Long, List<TimelineEventBO>> page = timelineEventService.getPage(pageNo, pageSize, profileId);
        return Result.success(page);
    }

    /**
     * 根据ID查询时间轴事件
     */
    @Operation(summary = "根据ID查询时间轴事件")
    @GetMapping("/{id}")
    public Result<TimelineEventBO> getById(@PathVariable Long id) {
        TimelineEventBO event = timelineEventService.getById(id);
        return Result.success(event);
    }

    /**
     * 创建时间轴事件
     */
    @Operation(summary = "创建时间轴事件")
    @PostMapping
    public Result<TimelineEventBO> create(@RequestBody TimelineEventBO eventBO) {
        TimelineEventBO created = timelineEventService.create(eventBO);
        return Result.success(created);
    }

    /**
     * 更新时间轴事件
     */
    @Operation(summary = "更新时间轴事件")
    @PutMapping
    public Result<Boolean> update(@RequestBody TimelineEventBO eventBO) {
        boolean updated = timelineEventService.update(eventBO);
        return Result.success(updated);
    }

    /**
     * 删除时间轴事件
     */
    @Operation(summary = "删除时间轴事件")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean deleted = timelineEventService.delete(id);
        return Result.success(deleted);
    }

    /**
     * 查询人物的完整时间轴
     */
    @Operation(summary = "查询人物的完整时间轴")
    @GetMapping("/profile/{profileId}")
    public Result<List<TimelineEventBO>> getTimelineByProfileId(@PathVariable Long profileId) {
        List<TimelineEventBO> timeline = timelineEventService.getTimelineByProfileId(profileId);
        return Result.success(timeline);
    }
}
