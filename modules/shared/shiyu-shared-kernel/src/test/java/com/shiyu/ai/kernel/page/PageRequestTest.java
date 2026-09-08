package com.shiyu.ai.kernel.page;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageRequestTest {

    @Test
    void pageNumberStartsAtOneAndPageSizeIsBounded() {
        assertThrows(IllegalArgumentException.class, () -> new PageRequest(0, 20));
        assertThrows(IllegalArgumentException.class, () -> new PageRequest(1, 0));
        assertThrows(IllegalArgumentException.class, () -> new PageRequest(1, 201));
    }

    @Test
    void calculatesDatabaseOffsetWithoutAmbiguity() {
        assertEquals(40L, new PageRequest(3, 20).offset());
    }
}
