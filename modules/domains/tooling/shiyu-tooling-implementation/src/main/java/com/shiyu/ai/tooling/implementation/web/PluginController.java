package com.shiyu.ai.tooling.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.tooling.implementation.plugin.registry.PluginRegistry;
import com.shiyu.ai.tooling.implementation.plugin.spi.PluginDescriptor;
import com.shiyu.ai.tooling.implementation.plugin.vo.PluginInfoVO;
import com.shiyu.ai.tooling.implementation.plugin.market.PluginMarketEntry;
import com.shiyu.ai.tooling.implementation.plugin.market.PluginMarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 插件系统 Controller
 *
 * 注意：所有参数均通过 @RequestParam 或 @RequestBody 传入，不使用 @PathVariable。
 */
@Slf4j
@Tag(name = "插件系统", description = "Plugin System")
@RestController
@RequestMapping("/api/tooling/plugins")
public class PluginController {

    private final PluginRegistry registry;
    private final PluginMarketService market;

    public PluginController(PluginRegistry pluginRegistry, PluginMarketService market) {
        this.registry = pluginRegistry;
        this.market = market;
    }

    @Operation(summary = "列出所有插件")
    @SaCheckPermission("plugin:list")
    @GetMapping
    public Result<List<PluginInfoVO>> listPlugins() {
        List<PluginInfoVO> plugins = registry.listPlugins().stream()
                .map(d -> {
                    PluginInfoVO vo = new PluginInfoVO();
                    vo.setId(d.getId());
                    vo.setName(d.getName());
                    vo.setVersion(d.getVersion());
                    vo.setDescription(d.getDescription());
                    vo.setState(d.getState().name());
                    vo.setLoadedAt(String.valueOf(d.getLoadedAt()));
                    return vo;
                })
                .collect(Collectors.toList());
        return Result.success(plugins);
    }

    @Operation(summary = "启动插件")
    @SaCheckPermission("plugin:start")
    @PostMapping("/start")
    public Result<Void> startPlugin(@RequestParam String pluginId) {
        try {
            registry.start(pluginId);
            return Result.success();
        } catch (Exception e) {
            return lifecycleFailure("启动", pluginId, e);
        }
    }

    @Operation(summary = "停止插件")
    @SaCheckPermission("plugin:stop")
    @PostMapping("/stop")
    public Result<Void> stopPlugin(@RequestParam String pluginId) {
        try {
            registry.stop(pluginId);
            return Result.success();
        } catch (Exception e) {
            return lifecycleFailure("停止", pluginId, e);
        }
    }

    @Operation(summary = "卸载插件")
    @SaCheckPermission("plugin:uninstall")
    @PostMapping("/uninstall")
    public Result<Void> uninstallPlugin(@RequestParam String pluginId) {
        try {
            registry.uninstall(pluginId);
            return Result.success();
        } catch (Exception e) {
            return lifecycleFailure("卸载", pluginId, e);
        }
    }

    @Operation(summary = "重新扫描插件目录")
    @SaCheckPermission("plugin:scan")
    @PostMapping("/scan")
    public Result<Void> rescan() {
        try {
            registry.scanAndLoadPlugins();
            return Result.success();
        } catch (Exception e) {
            return lifecycleFailure("扫描", null, e);
        }
    }

    private Result<Void> lifecycleFailure(String operation, String pluginId, Exception exception) {
        log.warn("插件{}失败: pluginIdLength={}, errorType={}, errorMessageLength={}",
                operation, pluginId == null ? 0 : pluginId.length(),
                exception.getClass().getSimpleName(), messageLength(exception));
        return Result.fail(operation + "失败，请稍后重试");
    }

    private int messageLength(Throwable exception) {
        return exception.getMessage() == null ? 0 : exception.getMessage().length();
    }

    @SaCheckPermission("plugin:market")
    @GetMapping("/market")
    public Result<List<PluginMarketEntry>> market() { return Result.success(market.list()); }

    @SaCheckPermission("plugin:market")
    @PostMapping("/market/publish")
    public Result<PluginMarketEntry> publish(@RequestBody PluginMarketEntry entry,
                                             @RequestParam(defaultValue = "false") boolean developmentMode) {
        return Result.success(market.publish(entry, developmentMode));
    }

    @SaCheckPermission("plugin:market")
    @PostMapping("/market/{pluginId}/disable")
    public Result<Void> disable(@PathVariable String pluginId) { market.disable(pluginId); return Result.success(); }
}

