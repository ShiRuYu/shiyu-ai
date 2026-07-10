package com.shiyu.ai.plugin.lifecycle;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.plugin.registry.PluginRegistry;
import com.shiyu.ai.plugin.spi.PluginDescriptor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "插件系统", description = "Plugin System")
@RestController
@RequestMapping("/plugin")
public class PluginController {

    private final PluginRegistry registry;

    public PluginController(PluginRegistry pluginRegistry) {
        this.registry = pluginRegistry;
    }

    @Operation(summary = "列出所有插件")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> listPlugins() {
        List<Map<String, Object>> plugins = registry.listPlugins().stream()
                .map(d -> Map.<String, Object>of(
                        "id", d.getId(),
                        "name", d.getName(),
                        "version", d.getVersion(),
                        "description", d.getDescription(),
                        "state", d.getState().name(),
                        "loadedAt", d.getLoadedAt()
                ))
                .collect(Collectors.toList());
        return Result.success(plugins);
    }

    @Operation(summary = "启动插件")
    @PostMapping("/{pluginId}/start")
    public Result<Void> startPlugin(@PathVariable String pluginId) {
        try {
            registry.start(pluginId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail("启动失败: " + e.getMessage());
        }
    }

    @Operation(summary = "停止插件")
    @PostMapping("/{pluginId}/stop")
    public Result<Void> stopPlugin(@PathVariable String pluginId) {
        try {
            registry.stop(pluginId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail("停止失败: " + e.getMessage());
        }
    }

    @Operation(summary = "卸载插件")
    @PostMapping("/{pluginId}/uninstall")
    public Result<Void> uninstallPlugin(@PathVariable String pluginId) {
        try {
            registry.uninstall(pluginId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail("卸载失败: " + e.getMessage());
        }
    }

    @Operation(summary = "重新扫描插件目录")
    @PostMapping("/scan")
    public Result<Void> rescan() {
        try {
            registry.scanAndLoadPlugins();
            return Result.success();
        } catch (Exception e) {
            return Result.fail("扫描失败: " + e.getMessage());
        }
    }
}
