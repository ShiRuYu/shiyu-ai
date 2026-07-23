package com.shiyu.ai.auth.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 用户级锁管理器
 * <p>
 * 保护 extInfo 的并发读写操作，避免不同线程同时读取-修改-写入导致的覆盖问题。
 * 使用 Stripe Lock 模式，每个 userId 分配一个可重入锁，自动清理空闲锁。
 */
public enum UserLockManager {
    INSTANCE;

    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    /**
     * 获取指定用户的锁
     */
    public ReentrantLock getLock(Long userId) {
        return lockMap.computeIfAbsent(userId, k -> new ReentrantLock());
    }

    /**
     * 获取锁并执行操作，自动释放
     */
    public <T> T executeWithLock(Long userId, LockSupplier<T> supplier) {
        ReentrantLock lock = getLock(userId);
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取锁并执行操作（无返回值），自动释放
     */
    public void executeWithLock(Long userId, Runnable runnable) {
        ReentrantLock lock = getLock(userId);
        lock.lock();
        try {
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 清理空闲锁（定期调用，释放不再使用的锁对象）
     */
    public void cleanUp() {
        lockMap.entrySet().removeIf(e -> !e.getValue().isLocked());
    }

    @FunctionalInterface
    public interface LockSupplier<T> {
        T get();
    }
}
