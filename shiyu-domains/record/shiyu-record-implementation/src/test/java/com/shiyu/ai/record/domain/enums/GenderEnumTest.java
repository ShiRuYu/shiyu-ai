package com.shiyu.ai.record.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenderEnumTest {
    @Test
    void resolvesKnownUnknownAndNullCodes() {
        assertEquals(GenderEnum.MALE, GenderEnum.fromCode(0));
        assertEquals(GenderEnum.FEMALE, GenderEnum.fromCode(1));
        assertEquals(GenderEnum.UNKNOWN, GenderEnum.fromCode(2));
        assertEquals(GenderEnum.UNKNOWN, GenderEnum.fromCode(99));
        assertEquals(GenderEnum.UNKNOWN, GenderEnum.fromCode(null));
        assertEquals("女", GenderEnum.getLabelByCode(1));
    }
}
