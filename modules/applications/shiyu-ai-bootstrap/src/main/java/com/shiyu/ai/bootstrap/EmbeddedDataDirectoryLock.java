package com.shiyu.ai.bootstrap;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;

/**
 * Prevents two application processes from opening the embedded database and
 * indexes for writing at the same time.
 */
final class EmbeddedDataDirectoryLock implements AutoCloseable {

    private static final String APP_HOME_ENV = "APP_HOME";
    private static final String APP_HOME_PROPERTY = "app.home";
    private static final Pattern ROOT_POM_PACKAGING =
            Pattern.compile("<packaging>\\s*pom\\s*</packaging>");

    private final FileChannel channel;
    private final FileLock lock;

    private EmbeddedDataDirectoryLock(FileChannel channel, FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    static EmbeddedDataDirectoryLock acquire() {
        String appHome = resolveAppHome(
                System.getProperty(APP_HOME_PROPERTY),
                System.getenv(APP_HOME_ENV),
                resolveDefaultAppHome(System.getProperty("user.dir", ".")));
        // The lock is acquired before Spring's EnvironmentPostProcessor runs.
        // Keep the system property aligned so all later non-Spring path users
        // resolve the same data directory.
        System.setProperty(APP_HOME_PROPERTY, appHome);
        Path dataRoot = Path.of(appHome, "data")
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(dataRoot);
            FileChannel channel = FileChannel.open(dataRoot.resolve(".shiyu-write.lock"),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            FileLock lock;
            try {
                lock = channel.tryLock();
            } catch (OverlappingFileLockException exception) {
                lock = null;
            }
            if (lock == null) {
                channel.close();
                throw new IllegalStateException("数据目录已被另一个写实例占用: " + dataRoot);
            }
            return new EmbeddedDataDirectoryLock(channel, lock);
        } catch (IOException exception) {
            throw new IllegalStateException("无法锁定嵌入式数据目录: " + dataRoot, exception);
        }
    }

    static String resolveAppHome(String systemProperty, String environmentValue, String fallback) {
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty;
        }
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }
        return fallback == null || fallback.isBlank() ? "." : fallback;
    }

    static String resolveDefaultAppHome(String userDir) {
        if (userDir == null || userDir.isBlank()) return ".";
        Path start = Path.of(userDir).toAbsolutePath().normalize();
        Path localRuntime = start.resolve("runtime").resolve("dev");
        if (Files.isDirectory(localRuntime)) return localRuntime.toString();

        Path current = start;
        for (int depth = 0; depth < 10 && current != null; depth++) {
            Path pom = current.resolve("pom.xml");
            if (Files.isRegularFile(pom) && isRootPom(pom)) {
                Path projectRuntime = current.resolve("runtime").resolve("dev");
                return Files.isDirectory(projectRuntime) ? projectRuntime.toString() : userDir;
            }
            current = current.getParent();
        }
        return userDir;
    }

    private static boolean isRootPom(Path pom) {
        try {
            return ROOT_POM_PACKAGING.matcher(Files.readString(pom)).find();
        } catch (IOException ignored) {
            return false;
        }
    }

    @Override
    public void close() {
        try {
            lock.release();
        } catch (IOException ignored) {
            // Process shutdown will release the operating-system lock.
        }
        try {
            channel.close();
        } catch (IOException ignored) {
            // Process shutdown will close the descriptor.
        }
    }
}
