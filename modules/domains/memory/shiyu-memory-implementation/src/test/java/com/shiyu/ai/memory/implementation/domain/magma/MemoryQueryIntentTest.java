package com.shiyu.ai.memory.implementation.domain.magma;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shiyu.ai.memory.contract.model.*;

import org.junit.jupiter.api.Test;

class MemoryQueryIntentTest {
    @Test
    void infersCausalQuestions() {
        assertEquals(MemoryQueryIntent.CAUSAL, MemoryQueryIntent.infer("why did this happen?"));
    }

    @Test
    void infersTemporalQuestions() {
        assertEquals(
                MemoryQueryIntent.TEMPORAL,
                MemoryQueryIntent.infer("what happened before the review?"));
    }

    @Test
    void defaultsToSemantic() {
        assertEquals(
                MemoryQueryIntent.SEMANTIC, MemoryQueryIntent.infer("similar study experience"));
    }
}
