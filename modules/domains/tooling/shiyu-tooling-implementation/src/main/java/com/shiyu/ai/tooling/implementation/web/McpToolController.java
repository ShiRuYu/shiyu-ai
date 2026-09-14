package com.shiyu.ai.tooling.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.tooling.contract.api.ToolService;
import com.shiyu.ai.tooling.implementation.tool.mcp.McpToolDescriptor;
import com.shiyu.ai.tooling.implementation.tool.mcp.McpToolRegistry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MCP 工具市场 Controller
 *
 * <p>注意：所有参数均通过 @RequestParam 或 @RequestBody 传入，不使用 @PathVariable。
 */
@Slf4j
@Tag(name = "MCP 工具市场", description = "MCP Tool Marketplace")
@RestController
@RequestMapping("/api/tooling/tools/mcp")
public class McpToolController {

    /**
     * registry 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final McpToolRegistry registry;
    /**
     * toolService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ToolService toolService;

    /**
     * {@code McpToolController} 创建并初始化当前类型实例。
     *
     * @param registry 参数值，用于执行当前操作。
     * @param toolService 参数值，用于执行当前操作。
     */
    public McpToolController(McpToolRegistry registry, ToolService toolService) {
        this.registry = registry;
        this.toolService = toolService;
    }

    /**
     * {@code listTools} 查询并返回当前操作所需的数据。
     *
     * @param category 参数值，用于执行当前操作。
     * @param tag 参数值，用于执行当前操作。
     * @param keyword 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "列出所有工具")
    @SaCheckPermission("tool:mcp:list")
    @GetMapping("/tools")
    public Result<List<McpToolDescriptor>> listTools(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword) {
        List<McpToolDescriptor> tools;
        if (keyword != null && !keyword.isBlank()) {
            tools = registry.searchTools(keyword);
        } else if (category != null && !category.isBlank()) {
            tools = registry.getToolsByCategory(category);
        } else if (tag != null && !tag.isBlank()) {
            tools = registry.getToolsByTag(tag);
        } else {
            tools = registry.listTools();
        }
        return Result.success(tools);
    }

    /**
     * {@code getTool} 查询并返回当前操作所需的数据。
     *
     * @param name 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "获取工具详情")
    @SaCheckPermission("tool:mcp:detail")
    @GetMapping("/tools/detail")
    public Result<McpToolDescriptor> getTool(@RequestParam String name) {
        McpToolDescriptor tool = registry.getTool(name);
        if (tool == null) {
            return Result.fail("工具不存在: " + name);
        }
        return Result.success(tool);
    }

    /**
     * {@code executeTool} 执行当前模块定义的业务流程。
     *
     * @param name 参数值，用于执行当前操作。
     * @param params 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "执行工具")
    @SaCheckPermission("tool:mcp:execute")
    @PostMapping("/tools/execute")
    public Result<Object> executeTool(
            @RequestParam String name, @RequestBody(required = false) Map<String, Object> params) {
        McpToolDescriptor tool = registry.getTool(name);
        if (tool == null) {
            return Result.fail("工具不存在: " + name);
        }
        ToolService.ToolExecutionResult result = toolService.execute(name, params);
        if (result.success()) {
            return Result.success(result.result());
        }
        log.warn(
                "MCP 工具执行失败: tool={}, errorMessageLength={}",
                name,
                result.errorMessage() == null ? 0 : result.errorMessage().length());
        return Result.fail("工具执行失败，请稍后重试");
    }

    /**
     * {@code getCategories} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "获取工具分类")
    @SaCheckPermission("tool:mcp:categories")
    @GetMapping("/categories")
    public Result<Set<String>> getCategories() {
        return Result.success(registry.getCategories());
    }

    /**
     * {@code getStats} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "获取工具统计")
    @SaCheckPermission("tool:mcp:stats")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(
                Map.of(
                        "totalTools", registry.size(),
                        "categories", registry.getCategories().size()));
    }
}
