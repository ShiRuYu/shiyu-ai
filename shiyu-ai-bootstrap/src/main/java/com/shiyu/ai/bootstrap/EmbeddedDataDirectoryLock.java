package com.shiyu.ai.bootstrap;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Prevents two application processes from opening the embedded database and
 * indexes for writing at the same time.
 */
final class EmbeddedDataDirectoryLock implements AutoCloseable {

    private final FileChannel channel;
    private final FileLock lock;

    private EmbeddedDataDirectoryLock(FileChannel channel, FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    static EmbeddedDataDirectoryLock acquire() {
        Path dataRoot = Path.of(System.getProperty("app.home", "."), "data")
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
