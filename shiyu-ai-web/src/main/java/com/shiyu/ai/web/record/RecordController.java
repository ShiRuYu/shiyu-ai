package com.shiyu.ai.web.record;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.record.service.RecordService;
import com.shiyu.ai.dal.record.bo.RecordBO;
import com.shiyu.ai.record.vo.RecordVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.PageQuery;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.record.request.RecordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "记录管理")
@SaCheckPermission("record:profile:list")
@RestController
@RequestMapping("/record/record")
public class RecordController {

    @Resource
    private RecordService recordService;

    @Operation(summary = "分页查询记录列表")
    @GetMapping("/list")
    public Result<PageData<RecordVO>> getPage(PageQuery pageQuery,
                                               @RequestParam(required = false) Long eventId) {
        Pair<Long, List<RecordBO>> page = recordService.getPage(pageQuery.getPageNum(), pageQuery.getPageSize(), eventId);
        return Result.success(new PageData<>(MapstructUtils.convert(page.getRight(), RecordVO.class), page.getLeft()));
    }

    @Operation(summary = "根据ID查询记录")
    @GetMapping("/detail")
    public Result<RecordVO> getById(@RequestParam Long id) {
        return Result.success(MapstructUtils.convert(recordService.getById(id), RecordVO.class));
    }

    @Operation(summary = "创建记录")
    @SaCheckPermission("record:profile:create")
    @PostMapping("/create")
    public Result<RecordVO> create(@Valid @RequestBody RecordRequest request) {
        RecordBO bo = new RecordBO();
        bo.setEventId(request.getEventId());
        bo.setContent(request.getContent());
        return Result.success(MapstructUtils.convert(recordService.create(bo), RecordVO.class));
    }

    @Operation(summary = "更新记录")
    @SaCheckPermission("record:profile:edit")
    @PostMapping("/update")
    public Result<Boolean> update(@RequestParam Long id, @Valid @RequestBody RecordRequest request) {
        RecordBO bo = recordService.getById(id);
        if (bo == null) return Result.fail("记录不存在");
        bo.setEventId(request.getEventId());
        bo.setContent(request.getContent());
        return Result.success(recordService.update(bo));
    }

    @Operation(summary = "删除记录")
    @SaCheckPermission("record:profile:delete")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long id) {
        return Result.success(recordService.delete(id));
    }
}
