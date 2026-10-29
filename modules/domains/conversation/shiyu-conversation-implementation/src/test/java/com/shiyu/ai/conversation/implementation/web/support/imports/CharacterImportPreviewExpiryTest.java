package com.shiyu.ai.conversation.implementation.web.support.imports;
import static org.junit.jupiter.api.Assertions.*;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeUnit;
/** 验证弃用的角色卡预览过期后被清理，且容量受限。 */
class CharacterImportPreviewExpiryTest {
    @Test void expiresWithoutConfirmationAndBoundsCapacity() {
        AtomicLong nanos = new AtomicLong();
        CharacterImportPreviewStore store = new CharacterImportPreviewStore(nanos::get);
        for (int i=0; i<1100; i++) store.issue(new TenantId(1), 1, new byte[0], "card", null);
        store.entries.cleanUp();
        assertTrue(store.entries.estimatedSize() <= 1000);
        nanos.set(TimeUnit.MINUTES.toNanos(11));
        store.entries.cleanUp();
        assertEquals(0, store.entries.estimatedSize());
    }
}
