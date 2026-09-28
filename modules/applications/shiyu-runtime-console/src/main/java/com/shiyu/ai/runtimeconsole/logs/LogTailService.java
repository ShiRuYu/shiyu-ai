package com.shiyu.ai.runtimeconsole.logs;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** 从当前运行时日志白名单中执行有界的尾部读取。 */
public final class LogTailService {

    private static final int MIN_READ_BYTES = 1024;
    private static final int MAX_READ_BYTES = 256 * 1024;
    private static final Map<String, String> LOG_FILES = Map.ofEntries(
            Map.entry("debug.log", "调试"),
            Map.entry("info.log", "应用"),
            Map.entry("warn.log", "警告"),
            Map.entry("error.log", "错误"),
            Map.entry("web.log", "Web 请求"),
            Map.entry("security.log", "安全审计"),
            Map.entry("ai.log", "模型"),
            Map.entry("knowledge.log", "知识库"),
            Map.entry("task.log", "任务"),
            Map.entry("integration.log", "集成"),
            Map.entry("database.log", "数据库"),
            Map.entry("performance.log", "性能"),
            Map.entry("tool.log", "工具"),
            Map.entry("audit.log", "审计"));

    private final Path logDirectory;

    public LogTailService(Path appHome) {
        this.logDirectory = appHome.toAbsolutePath().normalize().resolve("data/log");
    }

    public List<LogFile> listFiles() {
        List<LogFile> files = new ArrayList<>();
        for (Map.Entry<String, String> entry : LOG_FILES.entrySet()) {
            Path path = logDirectory.resolve(entry.getKey());
            try {
                if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                    BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                    files.add(new LogFile(entry.getKey(), entry.getValue(), attributes.size(), attributes.lastModifiedTime().toInstant()));
                }
            } catch (IOException ignored) {
                // 日志缺失或正在轮换时，暂不返回该文件，直到可以安全读取。
            }
        }
        return List.copyOf(files);
    }

    public LogChunk read(String fileName, String cursor, int requestedBytes, String level, String keyword)
            throws IOException {
        if (!LOG_FILES.containsKey(fileName)) {
            throw new IllegalArgumentException("Log file is not available");
        }
        Path file = resolveCurrentLog(fileName);
        if (!Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
            return new LogChunk(fileName, List.of(), null, false, false, 0);
        }

        Cursor parsed = cursor == null || cursor.isBlank() ? null : parseCursor(cursor);
        BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        long size = attributes.size();
        String fileKey = fileKey(attributes, file, parsed == null ? null : parsed.fileKey());
        int maxBytes = Math.max(MIN_READ_BYTES, Math.min(MAX_READ_BYTES, requestedBytes));
        boolean rotated = parsed != null && (!parsed.fileKey().equals(fileKey) || size < parsed.offset());
        long start = parsed == null || rotated
                ? Math.max(0, size - maxBytes)
                : Math.min(parsed.offset(), size);
        if (start > 0 && (parsed == null || rotated)) {
            start = skipPartialLine(file, start, size);
        }
        long bytesToRead = Math.min(Math.max(0, size - start), maxBytes);
        byte[] bytes = new byte[(int) bytesToRead];
        try (RandomAccessFile input = new RandomAccessFile(file.toFile(), "r")) {
            input.seek(start);
            input.readFully(bytes);
        }

        List<String> selected = filterLines(new String(bytes, StandardCharsets.UTF_8), level, keyword);
        long nextOffset = start + bytesToRead;
        String nextCursor = encodeCursor(fileKey, nextOffset);
        return new LogChunk(fileName, selected, nextCursor, rotated, start > 0 && (parsed == null || rotated), nextOffset);
    }

    private Path resolveCurrentLog(String fileName) throws IOException {
        Path normalizedRoot = logDirectory.toAbsolutePath().normalize();
        Path normalizedFile = normalizedRoot.resolve(fileName).normalize();
        if (!normalizedFile.startsWith(normalizedRoot) || !normalizedFile.getParent().equals(normalizedRoot)) {
            throw new IllegalArgumentException("Log path is outside the current log directory");
        }
        if (Files.isSymbolicLink(normalizedFile)) {
            throw new IllegalArgumentException("Symbolic log paths are not supported");
        }
        if (Files.exists(normalizedRoot) && Files.exists(normalizedFile)) {
            Path realRoot = normalizedRoot.toRealPath();
            Path realFile = normalizedFile.toRealPath();
            if (!realFile.getParent().equals(realRoot)) {
                throw new IllegalArgumentException("Log path resolves outside the current log directory");
            }
        }
        return normalizedFile;
    }

    private static long skipPartialLine(Path file, long start, long size) throws IOException {
        try (RandomAccessFile input = new RandomAccessFile(file.toFile(), "r")) {
            input.seek(start);
            while (input.getFilePointer() < size) {
                if (input.read() == '\n') {
                    return input.getFilePointer();
                }
            }
            return size;
        }
    }

    private static List<String> filterLines(String contents, String level, String keyword) {
        String minimumLevel = level == null ? "ALL" : level.toUpperCase(Locale.ROOT);
        String normalizedKeyword = keyword == null ? "" : keyword.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String raw : contents.split("\\R")) {
            String line = raw.stripTrailing();
            if (line.isEmpty()) {
                continue;
            }
            String lineLevel = findLevel(line);
            if (minimumLevelRank(lineLevel) < minimumLevelRank(minimumLevel)) {
                continue;
            }
            if (!normalizedKeyword.isEmpty() && !line.toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                continue;
            }
            result.add(line);
        }
        return List.copyOf(result);
    }

    private static String findLevel(String line) {
        for (String level : List.of("ERROR", "WARN", "INFO", "DEBUG", "TRACE")) {
            if (line.matches(".*\\b" + level + "\\s+.*")) {
                return level;
            }
        }
        return "ALL";
    }

    private static int minimumLevelRank(String level) {
        return switch (level) {
            case "TRACE" -> 1;
            case "DEBUG" -> 2;
            case "INFO" -> 3;
            case "WARN" -> 4;
            case "ERROR" -> 5;
            case "ALL" -> 0;
            default -> 0;
        };
    }

    private static String fileKey(BasicFileAttributes attributes, Path file, String previousKey) throws IOException {
        Object key = attributes.fileKey();
        if (key != null) {
            return "file:" + key;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            int sampleLength = previousHeadLength(previousKey, attributes.size());
            try (var stream = Files.newInputStream(file, LinkOption.NOFOLLOW_LINKS)) {
                byte[] head = stream.readNBytes(sampleLength);
                digest.update(head);
            }
            return "head:" + attributes.creationTime().toMillis() + ":" + sampleLength + ":"
                    + Base64.getUrlEncoder().withoutPadding().encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private static int previousHeadLength(String previousKey, long currentSize) {
        if (previousKey != null && previousKey.startsWith("head:")) {
            String[] parts = previousKey.split(":", 4);
            if (parts.length == 4) {
                try {
                    return Integer.parseInt(parts[2]);
                } catch (NumberFormatException ignored) {
                    // 旧游标格式错误时，回退到新的有界采样大小。
                }
            }
        }
        return (int) Math.min(16, currentSize);
    }

    private static String encodeCursor(String fileKey, long offset) {
        String key = Base64.getUrlEncoder().withoutPadding().encodeToString(fileKey.getBytes(StandardCharsets.UTF_8));
        return key + "." + offset;
    }

    private static Cursor parseCursor(String cursor) {
        int separator = cursor.lastIndexOf('.');
        if (separator < 1) {
            throw new IllegalArgumentException("Invalid log cursor");
        }
        try {
            String fileKey = new String(Base64.getUrlDecoder().decode(cursor.substring(0, separator)), StandardCharsets.UTF_8);
            long offset = Long.parseLong(cursor.substring(separator + 1));
            if (offset < 0) {
                throw new NumberFormatException("negative offset");
            }
            return new Cursor(fileKey, offset);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid log cursor", exception);
        }
    }

    /** 表示日志增量读取位置及文件状态。 */
    private record Cursor(String fileKey, long offset) {}

    /** 表示允许读取的日志文件及其元数据。 */
    public record LogFile(String name, String label, long sizeBytes, Instant modifiedAt) {}

    /** 表示一次日志读取返回的行、游标和轮换状态。 */
    public record LogChunk(
            String fileName,
            List<String> lines,
            String nextCursor,
            boolean rotated,
            boolean truncated,
            long nextOffset) {}
}
