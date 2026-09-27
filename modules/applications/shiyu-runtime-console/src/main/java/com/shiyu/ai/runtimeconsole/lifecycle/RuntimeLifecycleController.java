package com.shiyu.ai.runtimeconsole.lifecycle;

import java.util.Map;
import com.shiyu.ai.runtimeconsole.auth.ConsoleSessionStore;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Lifecycle operations are accepted only for a process owned by the desktop launcher. */
@RestController
@RequestMapping("/console/api/lifecycle")
public final class RuntimeLifecycleController {

    private final LauncherControlClient launcher;
    private final ConfigurableApplicationContext applicationContext;
    private final ConsoleSessionStore sessions;
    private final Environment environment;

    public RuntimeLifecycleController(
            LauncherControlClient launcher,
            ConfigurableApplicationContext applicationContext,
            ConsoleSessionStore sessions,
            Environment environment) {
        this.launcher = launcher;
        this.applicationContext = applicationContext;
        this.sessions = sessions;
        this.environment = environment;
    }

    @PostMapping("/restart")
    public ResponseEntity<Map<String, Object>> restart() {
        return requestLauncher("restart");
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stop() {
        return requestLauncher("stop");
    }

    @PostMapping("/internal-shutdown")
    public ResponseEntity<Map<String, String>> internalShutdown() {
        CompletableFuture.delayedExecutor(300, TimeUnit.MILLISECONDS)
                .execute(applicationContext::close);
        return ResponseEntity.accepted().body(Map.of("status", "SHUTTING_DOWN"));
    }

    @org.springframework.web.bind.annotation.GetMapping("/internal-link")
    public Map<String, String> internalLink() {
        int port = environment.getProperty("local.server.port", Integer.class,
                environment.getProperty("server.port", Integer.class, 9000));
        return Map.of("url", "http://127.0.0.1:" + port + "/console/#grant=" + sessions.issueOneTimeCode());
    }

    private ResponseEntity<Map<String, Object>> requestLauncher(String action) {
        if (!Boolean.getBoolean("shiyu.console.launcher-managed")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "IDEA / Jar 模式由 IDE 或当前进程管理，请手动重启或停止");
        }
        if (!launcher.available()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Windows 启动器控制通道不可用");
        }
        try {
            launcher.command(action);
            return ResponseEntity.accepted().body(Map.of(
                    "status", "ACCEPTED",
                    "managed", true,
                    "action", action,
                    "message", "Windows 启动器已接受" + ("restart".equals(action) ? "重启" : "停止") + "请求"));
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
        }
    }
}
