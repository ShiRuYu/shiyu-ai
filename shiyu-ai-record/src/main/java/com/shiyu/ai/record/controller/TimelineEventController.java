package com.shiyu.ai.record.controller;

import com.shiyu.ai.model.bo.TimelineEventBO;
import com.shiyu.ai.record.service.TimelineEventService;
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
 * 鏃堕棿杞翠簨浠舵帶鍒跺櫒
 */
@Tag(name = "鏃堕棿杞寸鐞?, description = "涓汉鎴愰暱璁板綍绯荤粺 - 鏃堕棿杞翠簨浠剁鐞?)
@RestController
@RequestMapping("/api/timeline")
public class TimelineEventController {

    @Resource
    private TimelineEventService timelineEventService;

    /**
     * 鍒嗛〉鏌ヨ鏃堕棿杞翠簨浠跺垪琛?
     */
    @Operation(summary = "鍒嗛〉鏌ヨ鏃堕棿杞翠簨浠跺垪琛?)
    @GetMapping("/page")
    public Result<PageData<TimelineEventBO>> getPage(
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam Long profileId) {
        Pair<Long, List<TimelineEventBO>> page = timelineEventService.getPage(pageNo, pageSize, profileId);
        PageData<TimelineEventBO> pageData = new PageData<>(page.getRight(), page.getLeft());
        return Result.success(pageData);
    }

    /**
     * 鏍规嵁ID鏌ヨ鏃堕棿杞翠簨浠?
     */
    @Operation(summary = "鏍规嵁ID鏌ヨ鏃堕棿杞翠簨浠?)
    @GetMapping("/{id}")
    public Result<TimelineEventBO> getById(@PathVariable Long id) {
        TimelineEventBO event = timelineEventService.getById(id);
        return Result.success(event);
    }

    /**
     * 鍒涘缓鏃堕棿杞翠簨浠?
     */
    @Operation(summary = "鍒涘缓鏃堕棿杞翠簨浠?)
    @PostMapping
    public Result<TimelineEventBO> create(@Valid @RequestBody TimelineEventBO eventBO) {
        TimelineEventBO created = timelineEventService.create(eventBO);
        return Result.success(created);
    }

    /**
     * 鏇存柊鏃堕棿杞翠簨浠?
     */
    @Operation(summary = "鏇存柊鏃堕棿杞翠簨浠?)
    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody TimelineEventBO eventBO) {
        boolean updated = timelineEventService.update(eventBO);
        return Result.success(updated);
    }

    /**
     * 鍒犻櫎鏃堕棿杞翠簨浠?
     */
    @Operation(summary = "鍒犻櫎鏃堕棿杞翠簨浠?)
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean deleted = timelineEventService.delete(id);
        return Result.success(deleted);
    }

    /**
     * 鏌ヨ浜虹墿鐨勫畬鏁存椂闂磋酱
     */
    @Operation(summary = "鏌ヨ浜虹墿鐨勫畬鏁存椂闂磋酱")
    @GetMapping("/profile/{profileId}")
    public Result<List<TimelineEventBO>> getTimelineByProfileId(@PathVariable Long profileId) {
        List<TimelineEventBO> timeline = timelineEventService.getTimelineByProfileId(profileId);
        return Result.success(timeline);
    }
}
