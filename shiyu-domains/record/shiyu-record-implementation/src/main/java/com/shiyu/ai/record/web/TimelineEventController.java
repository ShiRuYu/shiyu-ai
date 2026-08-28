package com.shiyu.ai.record.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.PageQuery;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.record.request.TimelineEventRequest;
import com.shiyu.ai.record.service.TimelineEventService;
import com.shiyu.ai.record.vo.TimelineEventVO;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "时间线事件管理")
@SaCheckPermission("record:timeline:list")
@RestController
@RequestMapping("/api/record/timeline")
public class TimelineEventController {
    @Resource private TimelineEventService timelineEventService;
    @GetMapping("/list") public Result<PageData<TimelineEventVO>> getPage(PageQuery query, @RequestParam(required = false) Long profileId) {
        var page = timelineEventService.pageView(ActorContextHttpAdapter.currentActor(), query.getPageNum(), query.getPageSize(), profileId);
        return Result.success(new PageData<>(page.getRight(), page.getLeft()));
    }
    @GetMapping("/detail") public Result<TimelineEventVO> getById(@RequestParam Long id) { return Result.success(timelineEventService.detailView(ActorContextHttpAdapter.currentActor(), id)); }
    @SaCheckPermission("record:timeline:create")
    @PostMapping("/create") public Result<TimelineEventVO> create(@Valid @RequestBody TimelineEventRequest request) { return Result.success(timelineEventService.create(ActorContextHttpAdapter.currentActor(), request)); }
    @SaCheckPermission("record:timeline:create")
    @PostMapping("/update") public Result<Boolean> update(@RequestParam Long id, @Valid @RequestBody TimelineEventRequest request) { return Result.success(timelineEventService.update(ActorContextHttpAdapter.currentActor(), id, request)); }
    @SaCheckPermission("record:timeline:delete")
    @PostMapping("/delete") public Result<Boolean> delete(@RequestParam Long id) { return Result.success(timelineEventService.delete(ActorContextHttpAdapter.currentActor(), id)); }
    @GetMapping("/profile") public Result<java.util.List<TimelineEventVO>> getTimelineByProfileId(@RequestParam Long profileId) { return Result.success(timelineEventService.timelineView(ActorContextHttpAdapter.currentActor(), profileId)); }
}
