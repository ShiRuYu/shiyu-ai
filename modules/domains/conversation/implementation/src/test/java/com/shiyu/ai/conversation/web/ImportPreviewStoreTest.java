package com.shiyu.ai.conversation.web;

import com.shiyu.ai.conversation.chat.CharacterCardV2;
import com.shiyu.ai.conversation.chat.ConversationExchangeCodec;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.lang.reflect.Field;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImportPreviewStoreTest {

    private static TenantId tenant(long value) { return new TenantId(value); }

    @Test
    void conversationPreviewIsBoundToTenantOwnerFormatAndPayloadAndIsSingleUse() {
        ConversationImportPreviewStore store = new ConversationImportPreviewStore();
        List<ConversationExchangeCodec.ImportedMessage> messages =
                List.of(new ConversationExchangeCodec.ImportedMessage("USER", "hello"));
        ConversationImportPreviewStore.Preview preview = store.issue(tenant(9), 7, "md", "payload", messages);

        assertEquals(messages, store.consume(tenant(9), 7, preview.token(), "markdown", "payload"));
        assertThrows(IllegalArgumentException.class,
                () -> store.consume(tenant(9), 7, preview.token(), "markdown", "payload"));

        ConversationImportPreviewStore.Preview wrong = store.issue(tenant(9), 7, "jsonl", "payload", messages);
        assertThrows(IllegalArgumentException.class,
                () -> store.consume(tenant(8), 7, wrong.token(), "jsonl", "payload"));
        ConversationImportPreviewStore.Preview swapped = store.issue(tenant(9), 7, "jsonl", "payload", messages);
        assertThrows(IllegalArgumentException.class,
                () -> store.consume(tenant(9), 7, swapped.token(), "jsonl", "other"));
        ConversationImportPreviewStore.Preview wrongOwner = store.issue(tenant(9), 7, "jsonl", "payload", messages);
        assertThrows(IllegalArgumentException.class,
                () -> store.consume(tenant(9), 8, wrongOwner.token(), "jsonl", "payload"));
        ConversationImportPreviewStore.Preview wrongFormat = store.issue(tenant(9), 7, "jsonl", "payload", messages);
        assertThrows(IllegalArgumentException.class,
                () -> store.consume(tenant(9), 7, wrongFormat.token(), "md", "payload"));
        ConversationImportPreviewStore.Preview nullFormat = store.issue(tenant(9), 7, null, "payload", messages);
        assertEquals(messages, store.consume(tenant(9), 7, nullFormat.token(), null, "payload"));
    }

    @Test
    void characterPreviewIsBoundToTenantUserFilenameAndBytes() {
        CharacterImportPreviewStore store = new CharacterImportPreviewStore();
        CharacterCardV2 card = new CharacterCardV2(null, "teacher", null, null, null, List.of(), null, Map.of(), 0);
        byte[] payload = {1, 2, 3};
        CharacterImportPreviewStore.Preview preview = store.issue(tenant(9), 7, payload, "card.png", card);

        assertEquals(card, store.consume(tenant(9), 7, preview.token(), payload, "card.png"));
        assertThrows(IllegalArgumentException.class,
                () -> store.consume(tenant(9), 7, preview.token(), payload, "card.png"));
        assertThrows(IllegalArgumentException.class,
                () -> store.consume(tenant(9), 7, " ", payload, "card.png"));

        CharacterImportPreviewStore.Preview wrong = store.issue(tenant(9), 7, payload, "card.png", card);
        assertThrows(IllegalArgumentException.class,
                () -> store.consume(tenant(9), 8, wrong.token(), payload, "card.png"));
        CharacterImportPreviewStore.Preview changed = store.issue(tenant(9), 7, payload, "card.png", card);
        assertThrows(IllegalArgumentException.class,
                () -> store.consume(tenant(9), 7, changed.token(), new byte[]{3, 2, 1}, "card.png"));
        CharacterImportPreviewStore.Preview renamed = store.issue(tenant(9), 7, payload, "card.png", card);
        assertThrows(IllegalArgumentException.class,
                () -> store.consume(tenant(9), 7, renamed.token(), payload, "other.png"));
    }

    @Test
    void rejectsBlankTokensAndCleansExpiredPendingPreviews() throws Exception {
        ConversationImportPreviewStore store = new ConversationImportPreviewStore();
        assertThrows(IllegalArgumentException.class,
                () -> store.consume(tenant(9), 7, " ", "jsonl", "payload"));
        Field field = ConversationImportPreviewStore.class.getDeclaredField("pending");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> pending = (Map<String, Object>) field.get(store);
        Class<?> pendingType = Class.forName(
                "com.shiyu.ai.conversation.web.ConversationImportPreviewStore$Pending");
        var constructor = pendingType.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object expired = constructor.newInstance(tenant(9), 7L, "jsonl", "digest", Instant.MIN,
                List.of());
        pending.put("expired", expired);
        store.issue(tenant(9), 7, "jsonl", "payload", List.of());
        assertFalse(pending.containsKey("expired"));
    }
}
