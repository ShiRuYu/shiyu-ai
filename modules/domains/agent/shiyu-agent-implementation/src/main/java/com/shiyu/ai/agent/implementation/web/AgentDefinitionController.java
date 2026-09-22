package com.shiyu.ai.agent.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.implementation.request.AgentRequest;
import com.shiyu.ai.agent.implementation.service.AgentAdminService;
import com.shiyu.ai.agent.implementation.service.AgentService;
import com.shiyu.ai.agent.implementation.vo.AgentDetailVO;
import com.shiyu.ai.agent.implementation.vo.AgentVO;
import com.shiyu.ai.agent.implementation.vo.NodeTypeMetaVO;
import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.foundation.vo.IdNameOptionVO;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 处理 智能体 Definition 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@Slf4j
@Tag(name = "Agent Definition", description = "Agent Definition")
@RestController
@RequestMapping("/api/agent/agents")
public class AgentDefinitionController {

    /**
     * agentAdminService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentAdminService agentAdminService;
    /**
     * agentService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentService agentService;

    /**
     * 执行 智能体 Definition 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param agentAdminService 用于完成本次业务处理的 agentAdminService 参数。
     * @param agentService 用于完成本次业务处理的 agentService 参数。
     */
    public AgentDefinitionController(
            AgentAdminService agentAdminService, AgentService agentService) {
        this.agentAdminService = agentAdminService;
        this.agentService = agentService;
    }

    // ======================== 来自 AgentAdminController ========================

    /**
     * {@code getPage} 查询并返回当前操作所需的数据。
     *
     * @param pageNo 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     * @param name 参数值，用于执行当前操作。
     * @param status 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Get Page")
    @SaCheckPermission("agent:admin:list")
    @GetMapping
    public Result<PageData<AgentVO>> getPage(
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        Pair<Long, List<AgentVO>> result =
                agentAdminService.getPage(actor(), pageNo, pageSize, name, status);
        return Result.success(new PageData<>(result.getRight(), result.getLeft()));
    }

    /**
     * 执行 智能体 Definition 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Id 用于定位目标业务对象的标识。
     */
    @Operation(summary = "Get by Id")
    @SaCheckPermission("agent:admin:list")
    @GetMapping("/{id}")
    public Result<AgentDetailVO> getById(@PathVariable Long id) {
        AgentDetailVO vo = agentAdminService.getById(actor(), id);
        if (vo == null) return Result.fail("Agent不存在");
        return Result.success(vo);
    }

    /**
     * 执行 智能体 Definition 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Create 用于完成本次业务处理的 Create 参数。
     */
    @Operation(summary = "Create")
    @SaCheckPermission("agent:admin:create")
    @PostMapping
    public Result<AgentVO> create(@Valid @RequestBody AgentRequest request) {
        try {
            AgentVO vo = agentAdminService.create(actor(), request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error(
                    "新增Agent失败: errorType={}, errorMessageLength={}",
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
            return Result.fail("新增失败");
        }
    }

    /**
     * 执行 智能体 Definition 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Update 用于完成本次业务处理的 Update 参数。
     */
    @Operation(summary = "Update")
    @SaCheckPermission("agent:admin:edit")
    @PutMapping("/{id}")
    public Result<AgentVO> update(@PathVariable Long id, @Valid @RequestBody AgentRequest request) {
        try {
            AgentVO vo = agentAdminService.update(actor(), id, request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error(
                    "修改Agent失败: errorType={}, errorMessageLength={}",
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
            return Result.fail("修改失败");
        }
    }

    /**
     * 执行 智能体 Definition 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Delete 用于完成本次业务处理的 Delete 参数。
     */
    @Operation(summary = "Delete")
    @SaCheckPermission("agent:admin:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            agentAdminService.deleteById(actor(), id);
            return Result.success();
        } catch (Exception e) {
            log.error(
                    "删除Agent失败: errorType={}, errorMessageLength={}",
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
            return Result.fail("删除失败");
        }
    }

    /**
     * 执行 智能体 Definition 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Status 用于完成本次业务处理的 Status 参数。
     */
    @Operation(summary = "Update Status")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/status")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        AgentRequest request = new AgentRequest();
        request.setStatus(status);
        try {
            agentAdminService.update(actor(), id, request);
            return Result.success();
        } catch (Exception e) {
            log.error(
                    "更新Agent状态失败: errorType={}, errorMessageLength={}",
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
            return Result.fail("更新失败");
        }
    }

    /**
     * 执行 智能体 Definition 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Options 用于完成本次业务处理的 Options 参数。
     */
    @Operation(summary = "List All Options")
    @SaCheckPermission("agent:admin:list")
    @GetMapping("/options")
    public Result<List<IdNameOptionVO>> listAllOptions() {
        return Result.success(agentAdminService.listAllOptions(actor()));
    }

    // ======================== 来自 AgentController（非执行部分） ========================

    /**
     * 执行 智能体 Definition 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Agent 用于完成本次业务处理的 Agent 参数。
     */
    @Operation(summary = "Register Agent")
    @SaCheckPermission("agent:admin:create")
    @PostMapping("/register")
    public Result<Map<String, Object>> registerAgent(@RequestBody RegisterAgentRequest request) {
        log.info(
                "收到 Agent 注册请求：agentIdPresent={}, namePresent={}",
                request.getAgentId() != null,
                request.getName() != null);
        try {
            AgentDefinition definition =
                    AgentDefinition.builder()
                            .agentId(request.getAgentId())
                            .name(request.getName())
                            .description(request.getDescription())
                            .createdAt(System.currentTimeMillis())
                            .updatedAt(System.currentTimeMillis())
                            .build();

            if (request.getGraph() != null) {
                com.shiyu.ai.agent.AgentVersion version =
                        com.shiyu.ai.agent.AgentVersion.builder()
                                .versionNumber(
                                        request.getVersionNumber() != null
                                                ? request.getVersionNumber()
                                                : "v1.0.0")
                                .description(request.getVersionDescription())
                                .graph(request.getGraph())
                                .createdAt(System.currentTimeMillis())
                                .build();
                definition.addVersion(version);
                definition.setCurrentVersion(version.getVersionNumber());
            }

            agentService.registerAgent(actor(), definition);
            return Result.success(Map.of("agentId", request.getAgentId()));
        } catch (Exception e) {
            log.error(
                    "Agent 注册失败：agentIdPresent={}, errorType={}, errorMessageLength={}",
                    request.getAgentId() != null,
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
            return Result.fail("Agent 注册失败");
        }
    }

    /**
     * 执行 智能体 Definition 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Agent 用于完成本次业务处理的 Agent 参数。
     */
    @Operation(summary = "Get Agent")
    @SaCheckPermission("agent:execute")
    @GetMapping("/definitions/{agentId}")
    public Result<AgentDefinition> getAgent(@PathVariable String agentId) {
        log.info("收到 Agent 查询请求：agentIdPresent={}", agentId != null);
        AgentDefinition definition = agentService.getAgent(actor(), agentId);
        if (definition == null) {
            return Result.fail("Agent 不存在");
        }
        return Result.success(definition);
    }

    /**
     * 执行 智能体 Definition 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Agent 用于完成本次业务处理的 Agent 参数。
     */
    @Operation(summary = "Delete Agent")
    @SaCheckPermission("agent:admin:delete")
    @DeleteMapping("/definitions/{agentId}")
    public Result<Void> deleteAgent(@PathVariable String agentId) {
        log.info("收到 Agent 删除请求：agentIdPresent={}", agentId != null);
        boolean success = agentService.unregisterAgent(actor(), agentId);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("Agent 不存在，删除失败");
        }
    }

    /**
     * 执行 智能体 Definition 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Version 用于完成本次业务处理的 Version 参数。
     */
    @Operation(summary = "Switch Version")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/version/switch")
    public Result<Void> switchVersion(@RequestParam String agentId, @RequestParam String version) {
        log.info(
                "收到 Agent 版本切换请求：agentIdPresent={}, versionPresent={}",
                agentId != null,
                version != null);
        boolean success = agentService.switchVersion(actor(), agentId, version);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("版本切换失败，版本不存在");
        }
    }

    /**
     * 执行 智能体 Definition 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Agents 用于完成本次业务处理的 Agents 参数。
     */
    @Operation(summary = "List Agents")
    @SaCheckPermission("agent:execute")
    @GetMapping("/definitions")
    public Result<List<AgentDefinition>> listAgents() {
        log.info("收到 Agent 列表查询请求");
        return Result.success(agentService.listAgents(actor()));
    }

    // ======================== 来自 NodeTypeController ========================

    /**
     * 执行 智能体 Definition 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Types 用于完成本次业务处理的 Types 参数。
     */
    @Operation(summary = "Get Node Types")
    @SaCheckPermission("agent:admin:list")
    @GetMapping("/node-types")
    public Result<List<NodeTypeMetaVO>> getNodeTypes() {
        return Result.success(agentAdminService.getNodeTypes());
    }

    /**
     * 执行 智能体 Definition 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Type 用于完成本次业务处理的 Type 参数。
     */
    @Operation(summary = "Get Node Type")
    @SaCheckPermission("agent:admin:list")
    @GetMapping("/node-types/detail")
    public Result<NodeTypeMetaVO> getNodeType(@RequestParam String nodeType) {
        List<NodeTypeMetaVO> types = agentAdminService.getNodeTypes();
        return types.stream()
                .filter(t -> t.getCode().equalsIgnoreCase(nodeType))
                .findFirst()
                .map(Result::success)
                .orElse(Result.fail("节点类型不存在: " + nodeType));
    }

    private ActorContext actor() {
        return ActorContextHttpAdapter.currentActor();
    }

    // ======================== 内嵌请求模型 ========================

    /**
     * 封装 Register 智能体 操作所需的请求条件和输入数据。
     */
    @lombok.Getter
    @lombok.Setter
    public static class RegisterAgentRequest {
        private String agentId;
        /**
         * 名称，表示当前对象中的对应属性。
         */
        private String name;
        /**
         * 描述，表示当前对象中的对应属性。
         */
        private String description;
        /**
         * versionNumber 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String versionNumber;
        /**
         * 版本描述，表示当前对象中的对应属性。
         */
        private String versionDescription;
        /**
         * 图结构，表示当前对象中的对应属性。
         */
        private com.shiyu.ai.agent.implementation.graph.Graph graph;
    }
}
