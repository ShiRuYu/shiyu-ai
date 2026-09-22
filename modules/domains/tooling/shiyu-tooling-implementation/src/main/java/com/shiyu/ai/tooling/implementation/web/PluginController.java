package com.shiyu.ai.tooling.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.foundation.api.Result;
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
 * 处理 插件 相关的 Web 请求，并将请求转换为应用服务调用。
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
     * 执行 插件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param pluginRegistry 用于完成本次业务处理的 pluginRegistry 参数。
     * @param market 用于完成本次业务处理的 market 参数。
     */
    public PluginController(PluginRegistry pluginRegistry, PluginMarketService market) {
        this.registry = pluginRegistry;
        this.market = market;
    }

    /**
     * 执行 插件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param summary 用于完成本次业务处理的 summary 参数。
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
     * 执行 插件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param summary 用于完成本次业务处理的 summary 参数。
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
     * 执行 插件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param summary 用于完成本次业务处理的 summary 参数。
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
     * 执行 插件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param summary 用于完成本次业务处理的 summary 参数。
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
     * 执行 插件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param summary 用于完成本次业务处理的 summary 参数。
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
     * 执行 插件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param market 用于完成本次业务处理的 market 参数。
     */
    @SaCheckPermission("plugin:market")
    @GetMapping("/market")
    public Result<List<PluginMarketEntry>> market() {
        return Result.success(market.list());
    }

    /**
     * 发布或发送 插件 相关业务数据，并返回处理结果。
     *
     * @param market 用于完成本次业务处理的 market 参数。
     * @return 返回 插件 相关操作生成的结果数据。
     */
    @SaCheckPermission("plugin:market")
    @PostMapping("/market/publish")
    public Result<PluginMarketEntry> publish(
            @RequestBody PluginMarketEntry entry,
            @RequestParam(defaultValue = "false") boolean developmentMode) {
        return Result.success(market.publish(entry, developmentMode));
    }

    /**
     * 执行 插件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param market 用于完成本次业务处理的 market 参数。
     */
    @SaCheckPermission("plugin:market")
    @PostMapping("/market/{pluginId}/disable")
    public Result<Void> disable(@PathVariable String pluginId) {
        market.disable(pluginId);
        return Result.success();
    }
}
