package com.shiyu.ai.aiagent.controller;

import com.shiyu.ai.aiagent.service.AgentAdminService;
import com.shiyu.ai.aiagent.vo.NodeTypeMetaVO;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@Slf4j
@Tag(name = "Node Type", description = "Node Type")
@RestController
@RequestMapping("/admin/agent/node-types")
public class NodeTypeController {

    private final AgentAdminService agentAdminService;

    public NodeTypeController(AgentAdminService agentAdminService) {
        this.agentAdminService = agentAdminService;
    }

    @Operation(summary = "Get Node Types")
    @GetMapping
    public Result<List<NodeTypeMetaVO>> getNodeTypes() {
        return Result.success(agentAdminService.getNodeTypes());
    }

    @Operation(summary = "Get Node Type")
    @GetMapping("/{nodeType}")
    public Result<NodeTypeMetaVO> getNodeType(@PathVariable String nodeType) {
        List<NodeTypeMetaVO> types = agentAdminService.getNodeTypes();
        return types.stream()
                .filter(t -> t.getCode().equalsIgnoreCase(nodeType))
                .findFirst()
                .map(Result::success)
                .orElse(Result.fail("节点类型不存在: " + nodeType));
    }
}
