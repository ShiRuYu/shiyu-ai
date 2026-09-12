package com.shiyu.ai.bootstrap.retention;

import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

/**
 * LogRetentionService 服务接口，负责执行应用领域相关业务操作。
 */
@Slf4j
@Service
public class LogRetentionService {

    /**
     * 配置属性，表示当前对象中的对应属性。
     */
    private final LogRetentionProperties properties;
    /**
     * historyRoot 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final Path historyRoot;

    /**
     * {@code LogRetentionService} 创建并初始化当前类型实例。
     *
     * @param properties 参数值，用于执行当前操作。
     */
    public LogRetentionService(LogRetentionProperties properties) {
        this.properties = properties;
        this.historyRoot =
                Path.of(System.getProperty("app.home", "."), "data", "log", "history")
                        .toAbsolutePath()
                        .normalize();
   }

    /**
     * {@code scheduledCleanup} 执行当前类型定义的业务操作。
     */
   @Scheduled(
           fixedDelayString = "${shiyu.retention.logs.interval-ms:3600000}",
           initialDelayString = "${shiyu.retention.logs.initial-delay-ms:120000}")
   public void scheduledCleanup() {
       if (properties.isEnabled()) {
            cleanup();
        }
    }

    void cleanup() {
        if (!Files.isDirectory(historyRoot)) return;
        try {
            List<Path> files;
            try (var stream = Files.walk(historyRoot)) {
                files =
                        stream.filter(Files::isRegularFile)
                                .sorted(Comparator.comparing(this::lastModified))
                                .toList();
            }
            Instant cutoff =
                    Instant.now().minus(Math.max(1, properties.getMaxAgeDays()), ChronoUnit.DAYS);
            for (Path file : files) {
                if (lastModified(file).isBefore(cutoff)) {
                    Files.deleteIfExists(file);
                }
            }

            files = existingFiles(files);
            long total = files.stream().mapToLong(this::size).sum();
            long limit = Math.max(0L, properties.getMaxTotalBytes());
            for (Path file : files) {
                if (total <= limit) break;
                long fileSize = size(file);
                if (Files.deleteIfExists(file)) total -= fileSize;
            }
        } catch (IOException exception) {
            log.warn("Log history cleanup failed: root={}", historyRoot, exception);
        }
    }

    private List<Path> existingFiles(List<Path> files) {
        return files.stream().filter(Files::isRegularFile).toList();
    }

    private Instant lastModified(Path file) {
        try {
            return Files.getLastModifiedTime(file).toInstant();
        } catch (IOException exception) {
            return Instant.EPOCH;
        }
    }

    private long size(Path file) {
        try {
            return Files.size(file);
        } catch (IOException exception) {
            return 0L;
        }
    }
}
