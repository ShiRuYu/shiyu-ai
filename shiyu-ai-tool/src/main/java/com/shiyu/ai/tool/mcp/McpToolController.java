package com.shiyu.ai.tool.mcp;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.tool.ToolService;
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
 * 注意：所有参数均通过 @RequestParam 或 @RequestBody 传入，不使用 @PathVariable。
 */
@Slf4j
@Tag(name = "MCP 工具市场", description = "MCP Tool Marketplace")
@RestController
@RequestMapping("/tool/mcp")
public class McpToolController {

    private final McpToolRegistry registry;
    private final ToolService toolService;

    public McpToolController(McpToolRegistry registry, ToolService toolService) {
        this.registry = registry;
        this.toolService = toolService;
    }

    @Operation(summary = "列出所有工具")
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

    @Operation(summary = "获取工具详情")
    @GetMapping("/tools/detail")
    public Result<McpToolDescriptor> getTool(@RequestParam String name) {
        McpToolDescriptor tool = registry.getTool(name);
        if (tool == null) {
            return Result.fail("工具不存在: " + name);
        }
        return Result.success(tool);
    }

    @Operation(summary = "执行工具")
    @PostMapping("/tools/execute")
    public Result<Object> executeTool(
            @RequestParam String name,
            @RequestBody(required = false) Map<String, Object> params) {
        McpToolDescriptor tool = registry.getTool(name);
        if (tool == null) {
            return Result.fail("工具不存在: " + name);
        }
        ToolService.ToolExecutionResult result = toolService.execute(name, params);
        if (result.success()) {
            return Result.success(result.result());
        }
        return Result.fail(result.errorMessage());
    }

    @Operation(summary = "获取工具分类")
    @GetMapping("/categories")
    public Result<Set<String>> getCategories() {
        return Result.success(registry.getCategories());
    }

    @Operation(summary = "获取工具统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(Map.of(
                "totalTools", registry.size(),
                "categories", registry.getCategories().size()
        ));
    }
}
