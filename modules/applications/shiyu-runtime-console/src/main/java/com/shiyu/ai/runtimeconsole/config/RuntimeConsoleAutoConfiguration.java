package com.shiyu.ai.runtimeconsole.config;

import com.shiyu.ai.common.web.config.WebPublicPathContributor;
import com.shiyu.ai.runtimeconsole.auth.ConsoleAccessFilter;
import com.shiyu.ai.runtimeconsole.auth.ConsoleSessionController;
import com.shiyu.ai.runtimeconsole.auth.ConsoleSessionStore;
import com.shiyu.ai.runtimeconsole.config.ConfigConsoleController;
import com.shiyu.ai.runtimeconsole.config.ConfigService;
import com.shiyu.ai.runtimeconsole.config.RuntimeConfigApplier;
import com.shiyu.ai.runtimeconsole.metrics.ConsoleHttpMetrics;
import com.shiyu.ai.runtimeconsole.metrics.ConsoleMetricsSampler;
import com.shiyu.ai.runtimeconsole.logs.LogTailService;
import com.shiyu.ai.runtimeconsole.web.ConsoleWebController;
import com.shiyu.ai.runtimeconsole.web.RuntimeConsoleController;
import com.shiyu.ai.runtimeconsole.lifecycle.LauncherControlClient;
import com.shiyu.ai.runtimeconsole.lifecycle.RuntimeLifecycleController;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import javax.sql.DataSource;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.boot.web.server.context.WebServerApplicationContext;

/** 装配本地运行时控制台的 Web、配置和生命周期组件。 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "shiyu.console", name = "enabled", havingValue = "true")
@Import({ConsoleSessionController.class, ConsoleWebController.class, RuntimeConsoleController.class, ConfigConsoleController.class, RuntimeLifecycleController.class})
public class RuntimeConsoleAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock consoleClock() {
        return Clock.systemUTC();
    }

    @Bean
    public ConsoleSessionStore consoleSessionStore(Clock consoleClock) {
        return new ConsoleSessionStore(consoleClock);
    }

    @Bean
    public LauncherControlClient launcherControlClient() {
        return LauncherControlClient.fromEnvironment();
    }

    @Bean
    @ConditionalOnMissingBean(MeterRegistry.class)
    public MeterRegistry consoleMeterRegistry() {
        return new SimpleMeterRegistry();
    }

    @Bean
    public ConsoleHttpMetrics consoleHttpMetrics(MeterRegistry registry) {
        return new ConsoleHttpMetrics(registry);
    }

    @Bean(destroyMethod = "close")
    public ConsoleMetricsSampler consoleMetricsSampler(
            ObjectProvider<DataSource> dataSources,
            ConsoleHttpMetrics httpMetrics,
            MeterRegistry registry,
            Environment environment) {
        Path appHome = Path.of(System.getProperty("app.home", ".")).toAbsolutePath().normalize();
        long sampleInterval = environment.getProperty("shiyu.console.sample-interval-ms", Long.class, 5_000L);
        return new ConsoleMetricsSampler(
                appHome, dataSources.orderedStream().findFirst().orElse(null), httpMetrics, registry, sampleInterval);
    }

    @Bean
    public LogTailService consoleLogTailService() {
        return new LogTailService(Path.of(System.getProperty("app.home", ".")));
    }

    @Bean
    public RuntimeConfigApplier runtimeConfigApplier(ConsoleMetricsSampler sampler) {
        return new RuntimeConfigApplier(sampler);
    }

    @Bean
    public ConfigService runtimeConfigService(
            ConfigSnapshotStore store, Environment environment, RuntimeConfigApplier applier) {
        return new ConfigService(store, environment, applier);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 20)
    public ConsoleAccessFilter consoleAccessFilter(
            ConsoleSessionStore sessions, ConsoleHttpMetrics httpMetrics) {
        return new ConsoleAccessFilter(sessions, httpMetrics);
    }

    @Bean
    public WebPublicPathContributor consolePublicPaths() {
        return () -> List.of("/console/**");
    }

    @Bean
    public ConfigSnapshotStore configSnapshotStore(ObjectMapper objectMapper) {
        Path appHome = Path.of(System.getProperty("app.home", ".")).toAbsolutePath().normalize();
        return new ConfigSnapshotStore(appHome, objectMapper, new DpapiSecretProtector());
    }

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper consoleSnapshotObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> consoleStartupListener(
            ConsoleSessionStore sessions, ConfigSnapshotStore configs, Environment environment) {
        return event -> {
            configs.markCurrentApplied();
            if (!environment.getProperty("shiyu.console.announce-startup-link", Boolean.class, true)) {
                return;
            }
            int port = event.getApplicationContext() instanceof WebServerApplicationContext webContext
                    ? webContext.getWebServer().getPort()
                    : environment.getProperty("server.port", Integer.class, 9000);
            String link = "http://127.0.0.1:" + port + "/console/#grant=" + sessions.issueOneTimeCode();
            // Windows 启动器读取这行机器可读文本以打开浏览器。
            // 由于 URL 包含授权凭据，不得将其复制到启动器诊断日志。
            System.out.println("SHIYU_CONSOLE_URL=" + link);
        };
    }
}
