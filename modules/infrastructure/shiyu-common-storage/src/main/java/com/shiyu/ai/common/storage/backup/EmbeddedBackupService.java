package com.shiyu.ai.common.storage.backup;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * {@code EmbeddedBackupService} 定义平台基础设施模块的应用服务能力，供上层用例调用。
 */
@Service
public class EmbeddedBackupService {

    /**
     * JDBC模板，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbcTemplate;
    /**
     * 数据根目录，表示当前对象中的对应属性。
     */
    private final Path dataRoot;
    /**
     * backupRoot 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final Path backupRoot;
    /**
     * 启用开关，表示当前对象中的对应属性。
     */
    private final boolean enabled;
    /**
     * hourlyRetention 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final int hourlyRetention;
    /**
     * dailyRetention 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final int dailyRetention;
    /**
     * maxTotalBytes 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final long maxTotalBytes;
    /**
     * manifestContributors 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final List<BackupManifestContributor> manifestContributors;

    /**
     * {@code EmbeddedBackupService} 创建并初始化当前类型实例。
     *
     * @param jdbcTemplate 参数值，用于执行当前操作。
     * @param dataDir 参数值，用于执行当前操作。
     * @param backupDir 参数值，用于执行当前操作。
     * @param enabled 参数值，用于执行当前操作。
     * @param hourlyRetention 参数值，用于执行当前操作。
     * @param dailyRetention 参数值，用于执行当前操作。
     * @param maxTotalBytes 参数值，用于执行当前操作。
     * @param manifestContributors 参数值，用于执行当前操作。
     */
    public EmbeddedBackupService(
            JdbcTemplate jdbcTemplate,
            @Value("${shiyu.storage.data-dir:${app.home}/data}") String dataDir,
            @Value("${shiyu.storage.backup.directory:${app.home}/data/backups}") String backupDir,
            @Value("${shiyu.storage.backup.enabled:true}") boolean enabled,
            @Value("${shiyu.storage.backup.hourly-retention:24}") int hourlyRetention,
            @Value("${shiyu.storage.backup.daily-retention:30}") int dailyRetention,
            @Value("${shiyu.storage.backup.max-total-bytes:21474836480}") long maxTotalBytes,
            ObjectProvider<BackupManifestContributor> manifestContributors) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataRoot = resolve(dataDir);
        this.backupRoot = resolve(backupDir);
        this.enabled = enabled;
        this.hourlyRetention = Math.max(0, hourlyRetention);
        this.dailyRetention = Math.max(0, dailyRetention);
        this.maxTotalBytes = Math.max(0, maxTotalBytes);
        this.manifestContributors = manifestContributors.orderedStream().toList();
   }

    /**
     * {@code scheduledBackup} 执行当前类型定义的业务操作。
     */
   @Scheduled(
           fixedDelayString = "${shiyu.storage.backup.interval-ms:3600000}",
           initialDelayString = "${shiyu.storage.backup.initial-delay-ms:300000}")
   public void scheduledBackup() {
       if (!enabled) return;
        try {
            backup();
            cleanupBackups();
        } catch (RuntimeException exception) {
            // A failed scheduled backup must not stop Spring's scheduler thread.
            org.slf4j.LoggerFactory.getLogger(EmbeddedBackupService.class)
                    .error("Scheduled embedded backup failed", exception);
        }
    }

    /**
     * {@code backup} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public BackupResult backup() {
        String timestamp =
                DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(OffsetDateTime.now());
        try {
            Files.createDirectories(backupRoot);
            Path h2Backup = backupRoot.resolve(".h2-" + timestamp + ".zip");
            Path snapshot = backupRoot.resolve("shiyu-backup-" + timestamp + ".zip");
            jdbcTemplate.execute("BACKUP TO '" + sqlPath(h2Backup) + "'");
            try (ZipOutputStream output =
                    new ZipOutputStream(
                            new BufferedOutputStream(Files.newOutputStream(snapshot)))) {
                addFile(output, h2Backup, "database/h2-backup.zip");
                addTree(output, dataRoot.resolve("files"), "files");
                addTree(output, dataRoot.resolve("models"), "models");
                addTree(output, dataRoot.resolve("index"), "index");
                addText(
                        output,
                        "manifest.properties",
                        "createdAt="
                                + OffsetDateTime.now()
                                + "\n"
                                + "formatVersion=1\n"
                                + "database=H2-MVStore\n"
                                + domainManifest());
            } finally {
                Files.deleteIfExists(h2Backup);
            }
            return new BackupResult(
                    snapshot.getFileName().toString(),
                    Files.size(snapshot),
                    OffsetDateTime.now().toString());
        } catch (Exception exception) {
            throw new ServiceException("创建嵌入式备份失败");
        }
    }

    /**
     * {@code restoreCheck} 执行当前类型定义的业务操作。
     *
     * @param fileName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public RestoreCheckResult restoreCheck(String fileName) {
        if (fileName == null || !fileName.equals(Path.of(fileName).getFileName().toString())) {
            throw new ServiceException("备份文件名非法");
        }
        Path snapshot = backupRoot.resolve(fileName).normalize();
        if (!snapshot.startsWith(backupRoot) || !Files.isRegularFile(snapshot)) {
            throw new ServiceException("备份文件不存在");
        }
        List<String> errors = new ArrayList<>();
        boolean database = false;
        boolean manifest = false;
        long entries = 0;
        try (ZipFile zip = new ZipFile(snapshot.toFile(), StandardCharsets.UTF_8)) {
            var iterator = zip.entries();
            while (iterator.hasMoreElements()) {
                ZipEntry entry = iterator.nextElement();
                entries++;
                database |= "database/h2-backup.zip".equals(entry.getName());
                manifest |= "manifest.properties".equals(entry.getName());
                try (var input = zip.getInputStream(entry)) {
                    input.transferTo(java.io.OutputStream.nullOutputStream());
                }
            }
        } catch (IOException exception) {
            errors.add("无法读取备份文件");
        }
        if (!database) errors.add("缺少 H2 一致性备份");
        if (!manifest) errors.add("缺少备份清单");
        return new RestoreCheckResult(errors.isEmpty(), entries, errors);
    }

    private String domainManifest() {
        StringBuilder manifest = new StringBuilder();
        for (BackupManifestContributor contributor : manifestContributors) {
            String contribution = contributor.contribute();
            if (contribution != null && !contribution.isBlank()) {
                manifest.append(contribution);
                if (!contribution.endsWith("\n")) manifest.append('\n');
            }
        }
        return manifest.toString();
    }

    /** Applies count and total-capacity retention to completed snapshots only. */
    void cleanupBackups() {
        try {
            Files.createDirectories(backupRoot);
            List<Path> backups;
            try (var paths = Files.list(backupRoot)) {
                backups =
                        paths.filter(Files::isRegularFile)
                                .filter(
                                        path ->
                                                path.getFileName()
                                                        .toString()
                                                        .startsWith("shiyu-backup-"))
                                .filter(path -> path.getFileName().toString().endsWith(".zip"))
                                .sorted(Comparator.comparing(this::lastModified).reversed())
                                .toList();
            }
            Set<Path> keep = new HashSet<>();
            backups.stream().limit(hourlyRetention).forEach(keep::add);
            Set<LocalDate> dailyDays = new HashSet<>();
            for (Path backup : backups) {
                LocalDate day = lastModified(backup).atZone(ZoneId.systemDefault()).toLocalDate();
                if (dailyDays.size() < dailyRetention && dailyDays.add(day)) keep.add(backup);
            }
            for (Path backup : backups) {
                if (!keep.contains(backup)) Files.deleteIfExists(backup);
            }
            if (maxTotalBytes <= 0) return;
            List<Path> remaining;
            try (var paths = Files.list(backupRoot)) {
                remaining =
                        paths.filter(Files::isRegularFile)
                                .filter(
                                        path ->
                                                path.getFileName()
                                                        .toString()
                                                        .startsWith("shiyu-backup-"))
                                .filter(path -> path.getFileName().toString().endsWith(".zip"))
                                .sorted(Comparator.comparing(this::lastModified))
                                .toList();
            }
            long total = remaining.stream().mapToLong(this::size).sum();
            for (Path backup : remaining) {
                if (total <= maxTotalBytes) break;
                long size = size(backup);
                if (Files.deleteIfExists(backup)) total -= size;
            }
        } catch (IOException exception) {
            throw new ServiceException("清理备份失败");
        }
    }

    /**
     * {@code status} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public Map<String, Object> status() {
        try {
            Files.createDirectories(dataRoot);
            var store = Files.getFileStore(dataRoot);
            return Map.of(
                    "dataRoot", dataRoot.toString(),
                    "usableBytes", store.getUsableSpace(),
                    "totalBytes", store.getTotalSpace(),
                    "singleWriter", true,
                    "database", "H2 File/MVStore");
        } catch (IOException exception) {
            throw new ServiceException("读取嵌入式运行状态失败");
        }
    }

    private void addTree(ZipOutputStream output, Path root, String prefix) throws IOException {
        if (!Files.isDirectory(root)) return;
        try (var paths = Files.walk(root)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                addFile(
                        output,
                        path,
                        prefix + "/" + root.relativize(path).toString().replace('\\', '/'));
            }
        }
    }

    private void addFile(ZipOutputStream output, Path source, String entryName) throws IOException {
        output.putNextEntry(new ZipEntry(entryName));
        try (var input = new BufferedInputStream(Files.newInputStream(source))) {
            input.transferTo(output);
        }
        output.closeEntry();
    }

    private void addText(ZipOutputStream output, String name, String content) throws IOException {
        output.putNextEntry(new ZipEntry(name));
        output.write(content.getBytes(StandardCharsets.UTF_8));
        output.closeEntry();
    }

    private Path resolve(String configured) {
        return Path.of(configured.replace("${app.home}", System.getProperty("app.home", ".")))
                .toAbsolutePath()
                .normalize();
    }

    private String sqlPath(Path path) {
        return path.toAbsolutePath().toString().replace('\\', '/').replace("'", "''");
    }

    private java.time.Instant lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (IOException exception) {
            return java.time.Instant.EPOCH;
        }
    }

    private long size(Path path) {
        try {
            return Files.size(path);
        } catch (IOException exception) {
            return 0L;
        }
    }

    /**
     * {@code BackupResult} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param fileName 文件名，表示该记录组件承载的数据。
     * @param size 大小，表示该记录组件承载的数据。
     * @param createdAt 创建时间，表示该记录组件承载的数据。
     */
    public record BackupResult(String fileName, long size, String createdAt) {}

    /**
     * {@code RestoreCheckResult} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param valid 是否有效，表示该记录组件承载的数据。
     * @param entries entries 属性，表示该记录组件承载的数据。
     * @param errors 错误列表，表示该记录组件承载的数据。
     */
    public record RestoreCheckResult(boolean valid, long entries, List<String> errors) {}
}
