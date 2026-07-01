package com.shiyu.ai.knowledge.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * RogueMap 数据文件管理器
 * <p>
 * 通过关闭标记文件检测上次是否为正常退出：
 * <ul>
 *   <li>正常退出 → {@code @PreDestroy} 写入 {@code .clean_shutdown} 标记，下次启动保留数据</li>
 *   <li>异常退出（JVM 崩溃）→ 标记不存在，启动时自动清理损坏的 mmap 文件</li>
 * </ul>
 * <p>
 * 数据根目录通过 {@code app.home} 配置注入，默认值为 {@code user.dir}。
 * 该值由 {@link com.shiyu.ai.common.core.config.AppHomeEnvironmentPostProcessor} 自动探测（从 work dir 向上找 pom.xml），
 * 也可通过环境变量 {@code APP_HOME} 覆盖，确保在任何环境下都能定位到项目根目录。
 */
@Slf4j
@Component
public class RogueMapFileManager {

    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 1000;

    /** 数据子目录（相对于 app.home） */
    private static final String DATA_SUB_DIR = "data/roguemap";

    private Path dataDir;
    private Path markerFile;

    @Value("${app.home:${user.dir}}")
    private String appHome;

    @PostConstruct
    public void init() {
        dataDir = Paths.get(appHome, DATA_SUB_DIR);
        markerFile = dataDir.resolve(".clean_shutdown");

        if (markerFile.toFile().exists()) {
            log.info("RogueMap: 检测到上次正常退出标记，保留数据文件 (dir={})", dataDir);
            markerFile.toFile().delete();
        } else {
            log.warn("RogueMap: 未检测到正常退出标记（上次可能异常退出），清理损坏的数据文件 (dir={})", dataDir);
            if (!cleanDataFiles()) {
                log.warn("RogueMap: 部分数据文件无法清理，尝试终止残留进程");
                killLingeringProcesses();
                // 再试一次
                if (!cleanDataFiles()) {
                    log.error("RogueMap: 数据文件被其他进程锁定，无法清理。请手动执行：");
                    log.error("  Stop-Process -Id $(Get-Process java | Where-Object MainWindowTitle -eq '').Id -Force");
                }
            }
        }
    }

    @PreDestroy
    public void markCleanShutdown() {
        try {
            Files.createDirectories(dataDir);
            if (markerFile.toFile().createNewFile()) {
                log.info("RogueMap: 已写入正常退出标记");
            }
        } catch (IOException e) {
            log.warn("RogueMap: 写入关闭标记失败", e);
        }
    }

    private boolean cleanDataFiles() {
        if (!Files.exists(dataDir)) {
            return true;
        }
        boolean allCleaned = true;
        try (Stream<Path> files = Files.list(dataDir)) {
            List<Path> toDelete = files
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.endsWith(".db") || name.endsWith(".mem");
                    })
                    .toList();

            for (Path path : toDelete) {
                if (!deleteWithRetry(path)) {
                    allCleaned = false;
                }
            }
        } catch (Exception e) {
            log.warn("清理 RogueMap 数据目录失败", e);
            allCleaned = false;
        }
        return allCleaned;
    }

    private boolean deleteWithRetry(Path path) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Files.deleteIfExists(path);
                log.info("已清理 RogueMap 数据文件: {}", path.getFileName());
                return true;
            } catch (IOException e) {
                if (i < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.warn("清理失败（重试{}次后）: {} - {}", i + 1, path.getFileName(), e.getMessage());
                }
            }
        }
        return false;
    }

    /**
     * 尝试终止残留的 Java 进程（非 IDE 主进程）
     */
    private static void killLingeringProcesses() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "taskkill", "/F", "/FI", "WINDOWTITLE eq ", "/IM", "java.exe");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.waitFor(3, TimeUnit.SECONDS);
            log.info("RogueMap: 已尝试终止残留 Java 进程");
        } catch (Exception e) {
            log.warn("RogueMap: 终止残留进程失败", e);
        }
    }
}
