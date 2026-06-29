package com.shiyu.ai.aiagent.controller;

import com.shiyu.ai.aiagent.service.AgentAdminService;
import com.shiyu.ai.aiagent.request.AgentRequest;
import com.shiyu.ai.aiagent.vo.AgentDetailVO;
import com.shiyu.ai.aiagent.vo.AgentVO;
import com.shiyu.ai.model.vo.IdNameOptionVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/agent")
public class AgentAdminController {

    private final AgentAdminService agentAdminService;

    public AgentAdminController(AgentAdminService agentAdminService) {
        this.agentAdminService = agentAdminService;
    }

    @GetMapping("/page")
    public Result<PageData<AgentVO>> getPage(
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        Pair<Long, List<AgentVO>> result = agentAdminService.getPage(pageNo, pageSize, name, status);
        return Result.success(new PageData<>(result.getRight(), result.getLeft()));
    }

    @GetMapping("/{id}")
    public Result<AgentDetailVO> getById(@PathVariable Long id) {
        AgentDetailVO vo = agentAdminService.getById(id);
        if (vo == null) return Result.fail("Agent不存在");
        return Result.success(vo);
    }

    @PostMapping
    public Result<AgentVO> create(@Valid @RequestBody AgentRequest request) {
        try {
            AgentVO vo = agentAdminService.create(request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("新增Agent失败", e);
            return Result.fail("新增失败");
        }
    }

    @PatchMapping("/{id}")
    public Result<AgentVO> update(@PathVariable Long id, @Valid @RequestBody AgentRequest request) {
        try {
            AgentVO vo = agentAdminService.update(id, request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("修改Agent失败", e);
            return Result.fail("修改失败");
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            agentAdminService.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除Agent失败", e);
            return Result.fail("删除失败");
        }
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
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
    @GetMapping("/list/all")
    public Result<List<IdNameOptionVO>> listAllOptions() {
        return Result.success(agentAdminService.listAllOptions());
    }
}
