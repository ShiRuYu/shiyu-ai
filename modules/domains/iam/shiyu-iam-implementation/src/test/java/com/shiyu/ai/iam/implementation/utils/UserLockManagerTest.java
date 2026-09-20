package com.shiyu.ai.iam.implementation.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 验证 用户 Lock Manager 相关功能、边界条件、异常路径和协作行为。
 */
class UserLockManagerTest {
    @Test
    void executesValueAndRunnableUnderUserLock() {
        UserLockManager manager = UserLockManager.INSTANCE;
        assertEquals("ok", manager.executeWithLock(912345L, () -> "ok"));
        AtomicBoolean called = new AtomicBoolean();
        manager.executeWithLock(912345L, () -> called.set(true));
        assertFalse(manager.getLock(912345L).isLocked());
        assertEquals(true, called.get());
        manager.cleanUp();
    }
}
