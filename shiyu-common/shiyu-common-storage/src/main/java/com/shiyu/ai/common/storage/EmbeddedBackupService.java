package com.shiyu.ai.common.storage;

import com.shiyu.ai.common.core.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.LocalDate;
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

@Service
public class EmbeddedBackupService {

    private final JdbcTemplate jdbcTemplate;
    private final Path dataRoot;
    private final Path backupRoot;
    private final boolean enabled;
    private final int hourlyRetention;
    private final int dailyRetention;
    private final long maxTotalBytes;

    public EmbeddedBackupService(
            JdbcTemplate jdbcTemplate,
            @Value("${shiyu.knowledge.data-dir:${app.home}/data}") String dataDir,
            @Value("${shiyu.knowledge.backup.directory:${app.home}/data/backups}") String backupDir,
            @Value("${shiyu.knowledge.backup.enabled:true}") boolean enabled,
            @Value("${shiyu.knowledge.backup.hourly-retention:24}") int hourlyRetention,
            @Value("${shiyu.knowledge.backup.daily-retention:30}") int dailyRetention,
            @Value("${shiyu.knowledge.backup.max-total-bytes:21474836480}") long maxTotalBytes) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataRoot = resolve(dataDir);
        this.backupRoot = resolve(backupDir);
        this.enabled = enabled;
        this.hourlyRetention = Math.max(0, hourlyRetention);
        this.dailyRetention = Math.max(0, dailyRetention);
        this.maxTotalBytes = Math.max(0, maxTotalBytes);
    }

    @Scheduled(fixedDelayString = "${shiyu.knowledge.backup.interval-ms:3600000}",
            initialDelayString = "${shiyu.knowledge.backup.initial-delay-ms:300000}")
    void scheduledBackup() {
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

    public BackupResult backup() {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
                .format(OffsetDateTime.now());
        try {
            Files.createDirectories(backupRoot);
            Path h2Backup = backupRoot.resolve(".h2-" + timestamp + ".zip");
            Path snapshot = backupRoot.resolve("shiyu-backup-" + timestamp + ".zip");
            jdbcTemplate.execute("BACKUP TO '" + sqlPath(h2Backup) + "'");
            try (ZipOutputStream output = new ZipOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(snapshot)))) {
                addFile(output, h2Backup, "database/h2-backup.zip");
                addTree(output, dataRoot.resolve("files"), "files");
                addTree(output, dataRoot.resolve("models"), "models");
                addTree(output, dataRoot.resolve("index"), "index");
                addText(output, "manifest.properties",
                        "createdAt=" + OffsetDateTime.now() + "\n"
                                + "formatVersion=1\n"
                                + "database=H2-MVStore\n"
                                + activeIndexManifest());
            } finally {
                Files.deleteIfExists(h2Backup);
            }
            return new BackupResult(snapshot.getFileName().toString(),
                    Files.size(snapshot), OffsetDateTime.now().toString());
        } catch (Exception exception) {
            throw new ServiceException("创建嵌入式备份失败: " + exception.getMessage());
        }
    }

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
            errors.add(exception.getMessage());
        }
        if (!database) errors.add("缺少 H2 一致性备份");
        if (!manifest) errors.add("缺少备份清单");
        return new RestoreCheckResult(errors.isEmpty(), entries, errors);
    }

    private String activeIndexManifest() {
        try {
            StringBuilder manifest = new StringBuilder();
            jdbcTemplate.query("SELECT tenant_id, id, active_index_version FROM knowledge_space "
                            + "WHERE del_flag = 0 ORDER BY tenant_id, id",
                    resultSet -> {
                        manifest.append("activeIndex.")
                                .append(resultSet.getLong("tenant_id"))
                                .append('.')
                                .append(resultSet.getLong("id"))
                                .append('=')
                                .append(resultSet.getLong("active_index_version"))
                                .append('\n');
                    });
            return manifest.toString();
        } catch (DataAccessException exception) {
            // Lightweight storage-only deployments may not include Knowledge tables.
            return "activeIndex.unavailable=true\n";
        }
    }

    /** Applies count and total-capacity retention to completed snapshots only. */
    void cleanupBackups() {
        try {
            Files.createDirectories(backupRoot);
            List<Path> backups;
            try (var paths = Files.list(backupRoot)) {
                backups = paths.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().startsWith("shiyu-backup-"))
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
                remaining = paths.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().startsWith("shiyu-backup-"))
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
            throw new ServiceException("清理备份失败: " + exception.getMessage());
        }
    }

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
            throw new ServiceException("读取嵌入式运行状态失败: " + exception.getMessage());
        }
    }

    private void addTree(ZipOutputStream output, Path root, String prefix) throws IOException {
        if (!Files.isDirectory(root)) return;
        try (var paths = Files.walk(root)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                addFile(output, path, prefix + "/" + root.relativize(path).toString().replace('\\', '/'));
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
                .toAbsolutePath().normalize();
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

    public record BackupResult(String fileName, long size, String createdAt) {
    }

    public record RestoreCheckResult(boolean valid, long entries, List<String> errors) {
    }
}
