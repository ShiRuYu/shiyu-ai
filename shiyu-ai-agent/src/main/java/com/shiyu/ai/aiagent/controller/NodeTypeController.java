package com.shiyu.ai.aiagent.controller;

import com.shiyu.ai.aiagent.service.AgentAdminService;
import com.shiyu.ai.aiagent.vo.NodeTypeMetaVO;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/agent/node-types")
public class NodeTypeController {

    private final AgentAdminService agentAdminService;

    public NodeTypeController(AgentAdminService agentAdminService) {
        this.agentAdminService = agentAdminService;
    }

    @GetMapping
    public Result<List<NodeTypeMetaVO>> getNodeTypes() {
        return Result.success(agentAdminService.getNodeTypes());
    }

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
