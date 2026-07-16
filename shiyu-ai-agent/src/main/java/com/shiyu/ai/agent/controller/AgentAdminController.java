package com.shiyu.ai.agent.controller;

import com.shiyu.ai.agent.service.AgentAdminService;
import com.shiyu.ai.agent.request.AgentRequest;
import com.shiyu.ai.agent.vo.AgentDetailVO;
import com.shiyu.ai.agent.vo.AgentVO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.PageQuery;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@Slf4j
@Tag(name = "Agent Admin", description = "Agent Admin")
@RestController
@RequestMapping("/agent/admin")
public class AgentAdminController {

    private final AgentAdminService agentAdminService;

    public AgentAdminController(AgentAdminService agentAdminService) {
        this.agentAdminService = agentAdminService;
    }

    @Operation(summary = "Get Page")
    @GetMapping("/list")
    public Result<PageData<AgentVO>> getPage(
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        Pair<Long, List<AgentVO>> result = agentAdminService.getPage(pageNo, pageSize, name, status);
        return Result.success(new PageData<>(result.getRight(), result.getLeft()));
    }

    @Operation(summary = "Get by Id")
    @GetMapping("/detail")
    public Result<AgentDetailVO> getById(@RequestParam Long id) {
        AgentDetailVO vo = agentAdminService.getById(id);
        if (vo == null) return Result.fail("Agent不存在");
        return Result.success(vo);
    }

    @Operation(summary = "Create")
    @PostMapping("/create")
    public Result<AgentVO> create(@Valid @RequestBody AgentRequest request) {
        try {
            AgentVO vo = agentAdminService.create(request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("新增Agent失败", e);
            return Result.fail("新增失败");
        }
    }

    @PostMapping("/update")
    public Result<AgentVO> update(@RequestParam Long id, @Valid @RequestBody AgentRequest request) {
        try {
            AgentVO vo = agentAdminService.update(id, request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("修改Agent失败", e);
            return Result.fail("修改失败");
        }
    }

    @Operation(summary = "Delete")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        try {
            agentAdminService.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除Agent失败", e);
            return Result.fail("删除失败");
        }
    }

    @Operation(summary = "Update Status")
    @PostMapping("/status")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam String status) {
        AgentRequest request = new AgentRequest();
        request.setStatus(status);
        try {
            agentAdminService.update(id, request);
            return Result.success();
        } catch (Exception e) {
            log.error("更新Agent状态失败", e);
            return Result.fail("更新失败");
        }
    }

    /**
     * 获取所有启用 Agent 选项（下拉选择用）
     */
    @Operation(summary = "List All Options")
    @GetMapping("/options")
    public Result<List<IdNameOptionVO>> listAllOptions() {
        return Result.success(agentAdminService.listAllOptions());
    }
}
