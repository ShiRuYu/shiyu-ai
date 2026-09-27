package com.shiyu.ai.runtimeconsole.launcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Restores a previous successful encrypted snapshot while the backend is offline. */
public final class OfflineConfigRecovery {

    private static final Pattern SNAPSHOT_NAME = Pattern.compile("snapshot-(\\d+)\\.json");
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public OfflineConfigRecovery(ObjectMapper objectMapper) {
        this(objectMapper, Clock.systemUTC());
    }

    OfflineConfigRecovery(ObjectMapper objectMapper, Clock clock) {
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public RestoreOutcome restoreLastApplied(Path appHome) throws IOException {
        Path directory = appHome.toAbsolutePath().normalize().resolve("data/runtime-console/config");
        long activeVersion = readPointer(directory.resolve("active-version"));
        long appliedVersion = readPointer(directory.resolve("last-applied-version"));
        if (appliedVersion < 1) {
            throw new IOException("没有可恢复的上次成功配置版本");
        }
        if (activeVersion == appliedVersion) {
            return new RestoreOutcome(appliedVersion, activeVersion);
        }
        Path source = directory.resolve("snapshot-" + appliedVersion + ".json");
        if (!Files.isRegularFile(source)) {
            throw new IOException("上次成功配置快照缺失：v" + appliedVersion);
        }
        ObjectNode restored = objectMapper.readValue(Files.readAllBytes(source), ObjectNode.class);
        if (restored.path("revision").asLong(-1) != appliedVersion) {
            throw new IOException("上次成功配置快照版本不匹配");
        }
        long nextRevision = Math.max(activeVersion, maxSnapshotRevision(directory)) + 1;
        restored.put("revision", nextRevision);
        restored.put("createdAtEpochMillis", clock.millis());
        restored.put("restoredFromRevision", appliedVersion);
        Files.createDirectories(directory);
        atomicWrite(directory.resolve("snapshot-" + nextRevision + ".json"),
                objectMapper.writeValueAsBytes(restored));
        atomicWrite(directory.resolve("active-version"), Long.toString(nextRevision).getBytes(StandardCharsets.UTF_8));
        return new RestoreOutcome(appliedVersion, nextRevision);
    }

    private static long readPointer(Path path) throws IOException {
        if (!Files.isRegularFile(path)) return 0;
        try {
            long revision = Long.parseLong(Files.readString(path, StandardCharsets.UTF_8).trim());
            if (revision < 0) throw new NumberFormatException("negative version");
            return revision;
        } catch (NumberFormatException exception) {
            throw new IOException("配置版本指针格式无效：" + path.getFileName(), exception);
        }
    }

    private static long maxSnapshotRevision(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) return 0;
        try (Stream<Path> files = Files.list(directory)) {
            return files.map(path -> {
                        Matcher matcher = SNAPSHOT_NAME.matcher(path.getFileName().toString());
                        if (!matcher.matches()) return 0L;
                        try { return Long.parseLong(matcher.group(1)); }
                        catch (NumberFormatException ignored) { return 0L; }
                    })
                    .mapToLong(Long::longValue)
                    .max()
                    .orElse(0);
        }
    }

    private static void atomicWrite(Path target, byte[] value) throws IOException {
        Path temporary = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".tmp");
        try {
            try (FileChannel channel = FileChannel.open(temporary,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                channel.write(ByteBuffer.wrap(value));
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

    public record RestoreOutcome(long restoredFromRevision, long activeRevision) {}
}
