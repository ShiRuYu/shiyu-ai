package com.shiyu.ai.conversation.chat;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CharacterAssetTest {
    @Test
    void pngBytesAreDefensivelyCopiedAndVisibilityIsValidated() {
        CharacterCardV2 card = new CharacterCardV2("v2", "A", "description", "scenario", "hello", List.<String>of(), "system", Map.<String, Object>of(), 2);
        byte[] original = {1, 2, 3};
        CharacterAsset asset = new CharacterAsset("id", 1, 2, card, "PUBLIC", original, Instant.now(), Instant.now());
        original[0] = 9;
        assertArrayEquals(new byte[]{1, 2, 3}, asset.pngData());
        byte[] returned = asset.pngData(); returned[1] = 9;
        assertArrayEquals(new byte[]{1, 2, 3}, asset.pngData());
        assertThrows(IllegalArgumentException.class, () -> new CharacterAsset("id", 1, 2, card, "UNKNOWN", null, Instant.now(), Instant.now()));
    }
}
