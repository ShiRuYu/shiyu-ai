package com.shiyu.ai.web.record;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.PageQuery;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.record.request.RecordRequest;
import com.shiyu.ai.record.service.RecordService;
import com.shiyu.ai.record.vo.RecordVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "记录管理")
@SaCheckPermission("record:profile:list")
@RestController
@RequestMapping("/record/record")
public class RecordController {
    @Resource private RecordService recordService;
    @GetMapping("/list") public Result<PageData<RecordVO>> getPage(PageQuery query, @RequestParam(required = false) Long eventId) {
        var page = recordService.pageView(query.getPageNum(), query.getPageSize(), eventId);
        return Result.success(new PageData<>(page.getRight(), page.getLeft()));
    }
    @GetMapping("/detail") public Result<RecordVO> getById(@RequestParam Long id) { return Result.success(recordService.detailView(id)); }
    @SaCheckPermission("record:profile:create")
    @PostMapping("/create") public Result<RecordVO> create(@Valid @RequestBody RecordRequest request) { return Result.success(recordService.create(request)); }
    @SaCheckPermission("record:profile:edit")
    @PostMapping("/update") public Result<Boolean> update(@RequestParam Long id, @Valid @RequestBody RecordRequest request) { return Result.success(recordService.update(id, request)); }
    @SaCheckPermission("record:profile:delete")
    @PostMapping("/delete") public Result<Boolean> delete(@RequestParam Long id) { return Result.success(recordService.delete(id)); }
}
