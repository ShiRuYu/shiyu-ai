package com.shiyu.ai.runtimeconsole.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** 以版本快照和原子活动版本指针持久化运行时配置。 */
public final class ConfigSnapshotStore {

    private final Path directory;
    private final ObjectMapper objectMapper;
    private final ConfigSecretProtector secretProtector;
    private final Clock clock;

    public ConfigSnapshotStore(Path appHome, ObjectMapper objectMapper, ConfigSecretProtector secretProtector) {
        this(appHome, objectMapper, secretProtector, Clock.systemUTC());
    }

    ConfigSnapshotStore(
            Path appHome, ObjectMapper objectMapper, ConfigSecretProtector secretProtector, Clock clock) {
        this.directory = appHome.toAbsolutePath().normalize().resolve("data/runtime-console/config");
        this.objectMapper = objectMapper;
        this.secretProtector = secretProtector;
        this.clock = clock;
    }

    public synchronized Snapshot current() {
        long version = readPointer("active-version");
        return version == 0 ? emptySnapshot() : readSnapshot(version);
    }

    public synchronized Snapshot save(
            long expectedVersion,
            Map<String, String> values,
            Map<String, String> secretReplacements,
            Set<String> clearSecrets) {
        Snapshot previous = current();
        requireExpected(expectedVersion, previous.revision());

        Map<String, String> mergedValues = new LinkedHashMap<>(previous.values());
        if (values != null) {
            mergedValues.putAll(values);
        }
        Map<String, String> encryptedSecrets = new LinkedHashMap<>(previous.encryptedSecrets());
        if (secretReplacements != null) {
            secretReplacements.forEach((key, value) -> encryptedSecrets.put(key, secretProtector.protect(value)));
        }
        if (clearSecrets != null) {
            clearSecrets.forEach(encryptedSecrets::remove);
        }

        Snapshot next = new Snapshot(
                1,
                previous.revision() + 1,
                clock.millis(),
                mergedValues,
                encryptedSecrets,
                null);
        writeSnapshot(next);
        writePointer("active-version", next.revision());
        return next;
    }

    public Map<String, String> readSecrets(Snapshot snapshot) {
        Map<String, String> values = new LinkedHashMap<>();
        snapshot.encryptedSecrets().forEach((key, value) -> values.put(key, secretProtector.unprotect(value)));
        return Map.copyOf(values);
    }

    /** 记录已成功完成应用启动的配置版本。 */
    public synchronized void markCurrentApplied() {
        long currentVersion = readPointer("active-version");
        if (currentVersion > 0) {
            writePointer("last-applied-version", currentVersion);
        }
    }

    /** 根据最近成功应用的版本创建新的待处理快照。 */
    public synchronized Snapshot restoreLastApplied(long expectedVersion) {
        Snapshot current = current();
        requireExpected(expectedVersion, current.revision());
        long appliedVersion = readPointer("last-applied-version");
        if (appliedVersion == 0) {
            throw new IllegalStateException("No successfully applied configuration is available to restore");
        }
        Snapshot applied = readSnapshot(appliedVersion);
        Snapshot restored = new Snapshot(
                1,
                current.revision() + 1,
                clock.millis(),
                applied.values(),
                applied.encryptedSecrets(),
                applied.revision());
        writeSnapshot(restored);
        writePointer("active-version", restored.revision());
        return restored;
    }

    private Snapshot emptySnapshot() {
        return new Snapshot(1, 0, clock.millis(), Map.of(), Map.of(), null);
    }

    private Snapshot readSnapshot(long version) {
        try {
            Path snapshot = directory.resolve("snapshot-" + version + ".json");
            return objectMapper.readValue(Files.readAllBytes(snapshot), Snapshot.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read console configuration version " + version, exception);
        }
    }

    private void writeSnapshot(Snapshot snapshot) {
        try {
            Files.createDirectories(directory);
            Path target = directory.resolve("snapshot-" + snapshot.revision() + ".json");
            atomicWrite(target, objectMapper.writeValueAsBytes(snapshot));
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot persist console configuration", exception);
        }
    }

    private long readPointer(String name) {
        Path pointer = directory.resolve(name);
        if (!Files.isRegularFile(pointer)) {
            return 0;
        }
        try {
            return Long.parseLong(Files.readString(pointer, StandardCharsets.UTF_8).trim());
        } catch (IOException | NumberFormatException exception) {
            throw new IllegalStateException("Cannot read console configuration pointer " + name, exception);
        }
    }

    private void writePointer(String name, long version) {
        try {
            Files.createDirectories(directory);
            atomicWrite(directory.resolve(name), Long.toString(version).getBytes(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot switch console configuration version", exception);
        }
    }

    private static void atomicWrite(Path target, byte[] content) throws IOException {
        Path temporary = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".tmp");
        try {
            try (FileChannel channel = FileChannel.open(
                    temporary, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                channel.write(ByteBuffer.wrap(content));
                channel.force(true);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void requireExpected(long expected, long actual) {
        if (expected != actual) {
            throw new VersionConflictException(expected, actual);
        }
    }

    /** 表示持久化配置快照的版本、值和密钥内容。 */
    public record Snapshot(
            long schemaVersion,
            long revision,
            long createdAtEpochMillis,
            Map<String, String> values,
            Map<String, String> encryptedSecrets,
            Long restoredFromRevision) {
        public Snapshot {
            values = values == null ? Map.of() : Map.copyOf(values);
            encryptedSecrets = encryptedSecrets == null ? Map.of() : Map.copyOf(encryptedSecrets);
        }
    }

    /** 表示保存配置时提交版本已过期。 */
    public static final class VersionConflictException extends RuntimeException {
        @Serial private static final long serialVersionUID = 1L;

        private final long expected;
        private final long actual;

        public VersionConflictException(long expected, long actual) {
            super("Configuration version conflict: expected " + expected + " but current version is " + actual);
            this.expected = expected;
            this.actual = actual;
        }

        public long expected() { return expected; }
        public long actual() { return actual; }
    }
}
