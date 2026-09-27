package com.shiyu.ai.runtimeconsole.web;

import com.shiyu.ai.runtimeconsole.logs.LogTailService;
import com.shiyu.ai.runtimeconsole.metrics.ConsoleMetricsSampler;
import com.shiyu.ai.runtimeconsole.metrics.MetricSnapshot;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/console/api")
public final class RuntimeConsoleController {

    private final Environment environment;
    private final ConsoleMetricsSampler metrics;
    private final LogTailService logs;
    private final ObjectProvider<DataSource> dataSources;

    public RuntimeConsoleController(
            Environment environment,
            ConsoleMetricsSampler metrics,
            LogTailService logs,
            ObjectProvider<DataSource> dataSources) {
        this.environment = environment;
        this.metrics = metrics;
        this.logs = logs;
        this.dataSources = dataSources;
    }

    @GetMapping("/runtime")
    public RuntimeStatus runtime() {
        String appHome = System.getProperty("app.home", ".");
        boolean managed = Boolean.getBoolean("shiyu.console.launcher-managed");
        String health = "UNKNOWN";
        String detail = "数据库连接尚未检测";
        DataSource dataSource = dataSources.orderedStream().findFirst().orElse(null);
        if (dataSource != null) {
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(2)) {
                    health = "UP";
                    detail = "应用与数据库连接正常";
                } else {
                    health = "DOWN";
                    detail = "数据库连接校验失败";
                }
            } catch (Exception exception) {
                health = "DOWN";
                detail = "数据库连接不可用";
            }
        }
        return new RuntimeStatus(
                environment.getProperty("shiyu.version", "unknown"),
                mode(managed),
                ProcessHandle.current().pid(),
                environment.getProperty("local.server.port", Integer.class,
                        environment.getProperty("server.port", Integer.class, 9000)),
                java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime(),
                Path.of(appHome).toAbsolutePath().normalize().toString(),
                health,
                detail,
                managed);
    }

    @GetMapping("/metrics")
    public MetricsResponse metrics() {
        return new MetricsResponse(metrics.snapshots());
    }

    @GetMapping("/logs/files")
    public LogFilesResponse logFiles() {
        return new LogFilesResponse(logs.listFiles());
    }

    @GetMapping("/logs/tail")
    public LogTailService.LogChunk tail(
            @RequestParam String file,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "65536") int maxBytes,
            @RequestParam(defaultValue = "ALL") String level,
            @RequestParam(defaultValue = "") String query) {
        try {
            return logs.read(file, cursor, maxBytes, level, query);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        } catch (java.io.IOException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Log file is unavailable");
        }
    }

    private String mode(boolean managed) {
        if (managed) return "Windows EXE";
        String command = System.getProperty("sun.java.command", "").toLowerCase(java.util.Locale.ROOT);
        if (command.endsWith(".jar") || command.contains(".jar ")) return "直接运行 Jar";
        return "IDEA / 直接运行";
    }

    public record MetricsResponse(List<MetricSnapshot> samples) {}
    public record LogFilesResponse(List<LogTailService.LogFile> files) {}
}
