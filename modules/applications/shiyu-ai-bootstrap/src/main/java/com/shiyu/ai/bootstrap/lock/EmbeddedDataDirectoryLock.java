package com.shiyu.ai.bootstrap.lock;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;

/**
 * 锁定嵌入式数据目录，避免多个进程同时写入数据库和索引。
 */
public final class EmbeddedDataDirectoryLock implements AutoCloseable {

    /**
     * 环境变量，表示当前对象中的对应属性。
     */
    private static final String APP_HOME_ENV = "APP_HOME";
    /**
     * 属性，表示当前对象中的对应属性。
     */
    private static final String APP_HOME_PROPERTY = "app.home";
    private static final Pattern ROOT_POM_PACKAGING =
            Pattern.compile("<packaging>\\s*pom\\s*</packaging>");

    /**
     * 通道，表示当前对象中的对应属性。
     */
    private final FileChannel channel;
    /**
     * 锁，表示当前对象中的对应属性。
     */
    private final FileLock lock;

    private EmbeddedDataDirectoryLock(FileChannel channel, FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    /**
     * {@code acquire} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static EmbeddedDataDirectoryLock acquire() {
        String appHome =
                resolveAppHome(
                        System.getProperty(APP_HOME_PROPERTY),
                        System.getenv(APP_HOME_ENV),
                        resolveDefaultAppHome(System.getProperty("user.dir", ".")));
        System.setProperty(APP_HOME_PROPERTY, appHome);
        Path dataRoot = Path.of(appHome, "data").toAbsolutePath().normalize();
        try {
            Files.createDirectories(dataRoot);
            FileChannel channel =
                    FileChannel.open(
                            dataRoot.resolve(".shiyu-write.lock"),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE);
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

    /**
     * {@code close} 释放或移除当前操作涉及的资源。
     */
    @Override
    public void close() {
        try {
            lock.release();
        } catch (IOException ignored) {
        }
        try {
            channel.close();
        } catch (IOException ignored) {
        }
    }
}
