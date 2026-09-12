package com.shiyu.ai.tooling.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.tooling.implementation.plugin.market.model.PluginMarketEntry;
import com.shiyu.ai.tooling.implementation.plugin.market.service.PluginMarketService;
import com.shiyu.ai.tooling.implementation.plugin.registry.PluginRegistry;
import com.shiyu.ai.tooling.implementation.plugin.vo.PluginInfoVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 插件系统 Controller
 *
 * <p>注意：所有参数均通过 @RequestParam 或 @RequestBody 传入，不使用 @PathVariable。
 */
@Slf4j
@Tag(name = "插件系统", description = "Plugin System")
@RestController
@RequestMapping("/api/tooling/plugins")
public class PluginController {

    /**
     * registry 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final PluginRegistry registry;
    /**
     * market 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final PluginMarketService market;

    /**
     * {@code PluginController} 创建并初始化当前类型实例。
     *
     * @param pluginRegistry 参数值，用于执行当前操作。
     * @param market 参数值，用于执行当前操作。
     */
    public PluginController(PluginRegistry pluginRegistry, PluginMarketService market) {
        this.registry = pluginRegistry;
        this.market = market;
    }

    /**
     * {@code listPlugins} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "列出所有插件")
    @SaCheckPermission("plugin:list")
    @GetMapping
    public Result<List<PluginInfoVO>> listPlugins() {
        List<PluginInfoVO> plugins =
                registry.listPlugins().stream()
                        .map(
                                d -> {
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

    /**
     * {@code startPlugin} 执行当前类型定义的业务操作。
     *
     * @param pluginId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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

    /**
     * {@code stopPlugin} 执行当前类型定义的业务操作。
     *
     * @param pluginId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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

    /**
     * {@code uninstallPlugin} 执行当前类型定义的业务操作。
     *
     * @param pluginId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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

    /**
     * {@code rescan} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
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
        log.warn(
                "插件{}失败: pluginIdLength={}, errorType={}, errorMessageLength={}",
                operation,
                pluginId == null ? 0 : pluginId.length(),
                exception.getClass().getSimpleName(),
                messageLength(exception));
        return Result.fail(operation + "失败，请稍后重试");
    }

    private int messageLength(Throwable exception) {
        return exception.getMessage() == null ? 0 : exception.getMessage().length();
    }

    /**
     * {@code market} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("plugin:market")
    @GetMapping("/market")
    public Result<List<PluginMarketEntry>> market() {
        return Result.success(market.list());
    }

    /**
     * {@code publish} 执行当前模块定义的业务流程。
     *
     * @param entry 参数值，用于执行当前操作。
     * @param developmentMode 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("plugin:market")
    @PostMapping("/market/publish")
    public Result<PluginMarketEntry> publish(
            @RequestBody PluginMarketEntry entry,
            @RequestParam(defaultValue = "false") boolean developmentMode) {
        return Result.success(market.publish(entry, developmentMode));
    }

    /**
     * {@code disable} 执行当前类型定义的业务操作。
     *
     * @param pluginId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("plugin:market")
    @PostMapping("/market/{pluginId}/disable")
    public Result<Void> disable(@PathVariable String pluginId) {
        market.disable(pluginId);
        return Result.success();
    }
}
