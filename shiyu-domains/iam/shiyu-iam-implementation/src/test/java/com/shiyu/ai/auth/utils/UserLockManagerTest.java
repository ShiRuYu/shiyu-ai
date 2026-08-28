package com.shiyu.ai.auth.utils;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
