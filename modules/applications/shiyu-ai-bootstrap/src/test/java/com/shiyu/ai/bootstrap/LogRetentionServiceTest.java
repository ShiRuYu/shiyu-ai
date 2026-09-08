package com.shiyu.ai.bootstrap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogRetentionServiceTest {

    @TempDir
    Path tempDirectory;

    private String previousAppHome;

    @AfterEach
    void restoreAppHome() {
        if (previousAppHome == null) {
            System.clearProperty("app.home");
        } else {
            System.setProperty("app.home", previousAppHome);
        }
    }

    @Test
    void removesExpiredFilesAndThenEnforcesTotalSize() throws Exception {
        previousAppHome = System.getProperty("app.home");
        System.setProperty("app.home", tempDirectory.toString());

        Path root = tempDirectory.resolve("data/log/history");
        Files.createDirectories(root);
        Path expired = Files.writeString(root.resolve("expired.log"), "expired");
        Files.setLastModifiedTime(expired, FileTime.from(Instant.now().minus(10, ChronoUnit.DAYS)));
        Path oldest = Files.writeString(root.resolve("oldest.log"), "1234");
        Path newest = Files.writeString(root.resolve("newest.log"), "5678");
        Files.setLastModifiedTime(oldest, FileTime.from(Instant.now().minus(2, ChronoUnit.DAYS)));
        Files.setLastModifiedTime(newest, FileTime.from(Instant.now().minus(1, ChronoUnit.DAYS)));

        LogRetentionProperties properties = new LogRetentionProperties();
        properties.setMaxAgeDays(5);
        properties.setMaxTotalBytes(4);
        LogRetentionService service = new LogRetentionService(properties);
        service.cleanup();

        assertFalse(Files.exists(expired));
        assertFalse(Files.exists(oldest));
        assertTrue(Files.exists(newest));
    }
}
