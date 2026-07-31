package com.shiyu.ai.web.knowledge;

import com.shiyu.ai.common.core.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@Service
public class EmbeddedBackupService {

    private final JdbcTemplate jdbcTemplate;
    private final Path dataRoot;
    private final Path backupRoot;

    public EmbeddedBackupService(
            JdbcTemplate jdbcTemplate,
            @Value("${shiyu.knowledge.data-dir:${app.home}/data}") String dataDir,
            @Value("${shiyu.knowledge.backup.directory:${app.home}/data/backups}") String backupDir) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataRoot = resolve(dataDir);
        this.backupRoot = resolve(backupDir);
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
                                + "database=H2-MVStore\n");
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

    public record BackupResult(String fileName, long size, String createdAt) {
    }

    public record RestoreCheckResult(boolean valid, long entries, List<String> errors) {
    }
}
