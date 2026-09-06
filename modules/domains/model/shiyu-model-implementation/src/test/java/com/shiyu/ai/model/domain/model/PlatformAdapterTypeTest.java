package com.shiyu.ai.model.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlatformAdapterTypeTest {

    @Test
    void defaultsBlankValuesToOpenAiCompatible() {
        assertEquals(PlatformAdapterType.OPENAI_COMPATIBLE, PlatformAdapterType.parse(null));
        assertEquals(PlatformAdapterType.OPENAI_COMPATIBLE, PlatformAdapterType.parse(""));
        assertEquals(PlatformAdapterType.OPENAI_COMPATIBLE, PlatformAdapterType.parse("  "));
    }

    @Test
    void parsesOnlySupportedAdapterTypes() {
        assertEquals(PlatformAdapterType.OLLAMA, PlatformAdapterType.parse("ollama"));
    }

}
