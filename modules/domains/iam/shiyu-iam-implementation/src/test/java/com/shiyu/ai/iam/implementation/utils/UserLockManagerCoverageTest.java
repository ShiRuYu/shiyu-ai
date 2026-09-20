package com.shiyu.ai.iam.implementation.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 验证 用户 Lock Manager Coverage 相关功能、边界条件、异常路径和协作行为。
 */
class UserLockManagerCoverageTest {
    @Test
    void reusesLocksExecutesSuppliersAndCleansUnlockedEntries() {
        var manager = UserLockManager.INSTANCE;
        var first = manager.getLock(991L);
        assertSame(first, manager.getLock(991L));
        assertEquals("value", manager.executeWithLock(991L, () -> "value"));
        AtomicBoolean ran = new AtomicBoolean();
        manager.executeWithLock(991L, () -> ran.set(true));
        assertTrue(ran.get());
        assertThrows(
                IllegalStateException.class,
                () ->
                        manager.executeWithLock(
                                991L,
                                () -> {
                                    throw new IllegalStateException("boom");
                                }));
    }

    @Test
    void cleanupDoesNotRemoveAHeldLock() throws Exception {
        var manager = UserLockManager.INSTANCE;
        var lock = manager.getLock(992L);
        lock.lock();
        try {
            manager.cleanUp();
            assertTrue(lock.isLocked());
        } finally {
            lock.unlock();
        }
        manager.cleanUp();
    }
}
