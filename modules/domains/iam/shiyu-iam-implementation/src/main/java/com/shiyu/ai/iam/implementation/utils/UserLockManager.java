package com.shiyu.ai.iam.implementation.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 定义 用户 Lock 可用的枚举值及其业务语义。
 */
public enum UserLockManager {
    INSTANCE;

    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    /** 获取指定用户的锁 */
    public ReentrantLock getLock(Long userId) {
        return lockMap.computeIfAbsent(userId, k -> new ReentrantLock());
    }

    /** 获取锁并执行操作，自动释放 */
    public <T> T executeWithLock(Long userId, LockSupplier<T> supplier) {
        ReentrantLock lock = getLock(userId);
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    /** 获取锁并执行操作（无返回值），自动释放 */
    public void executeWithLock(Long userId, Runnable runnable) {
        ReentrantLock lock = getLock(userId);
        lock.lock();
        try {
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    /** 清理空闲锁（定期调用，释放不再使用的锁对象） */
    public void cleanUp() {
        lockMap.entrySet().removeIf(e -> !e.getValue().isLocked());
    }

    /**
     * 定义 Lock Supplier 相关的协作契约和调用边界。
     */
    @FunctionalInterface
    public interface LockSupplier<T> {
        /**
         * 查询 Lock Supplier 相关业务数据，并返回处理结果。
         *
         * @return 返回 Lock Supplier 相关操作生成的结果数据。
         */
        T get();
    }
}
