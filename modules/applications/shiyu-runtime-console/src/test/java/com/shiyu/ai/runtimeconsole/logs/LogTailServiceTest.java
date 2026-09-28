package com.shiyu.ai.runtimeconsole.logs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** 覆盖日志增量读取、过滤、轮换和路径安全行为。 */
class LogTailServiceTest {

    @TempDir Path appHome;

    @Test
    void returnsTailThenIncrementalBytesAndFiltersWithoutOpeningOtherFiles() throws Exception {
        Path log = appHome.resolve("data/log/info.log");
        Files.createDirectories(log.getParent());
        Files.writeString(log, "2026 INFO startup ready\n2026 WARN task delayed\n", StandardCharsets.UTF_8);
        LogTailService service = new LogTailService(appHome);

        LogTailService.LogChunk first = service.read("info.log", null, 4096, "WARN", "task");
        Files.writeString(log, "2026 ERROR task failed\n", StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
        LogTailService.LogChunk next = service.read("info.log", first.nextCursor(), 4096, "ERROR", "failed");

        assertEquals(1, first.lines().size());
        assertTrue(first.lines().get(0).contains("task delayed"));
        assertEquals(1, next.lines().size());
        assertTrue(next.lines().get(0).contains("task failed"));
        assertFalse(next.rotated());
    }

    @Test
    void detectsRotationAndRejectsNonAllowlistedPaths() throws Exception {
        Path log = appHome.resolve("data/log/info.log");
        Files.createDirectories(log.getParent());
        Files.writeString(log, "old\n");
        LogTailService service = new LogTailService(appHome);
        LogTailService.LogChunk before = service.read("info.log", null, 4096, "ALL", "");
        Files.delete(log);
        Files.writeString(log, "new\n", StandardCharsets.UTF_8);

        LogTailService.LogChunk after = service.read("info.log", before.nextCursor(), 4096, "ALL", "");

        assertTrue(after.rotated());
        assertEquals("new", after.lines().get(0));
        assertThrows(IllegalArgumentException.class, () -> service.read("../config/active-version", null, 4096, "ALL", ""));
    }
}
