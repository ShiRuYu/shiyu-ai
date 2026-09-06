package com.shiyu.ai.memory.magma;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemoryQueryIntentTest {
    @Test void infersCausalQuestions() { assertEquals(MemoryQueryIntent.CAUSAL, MemoryQueryIntent.infer("why did this happen?")); }
    @Test void infersTemporalQuestions() { assertEquals(MemoryQueryIntent.TEMPORAL, MemoryQueryIntent.infer("what happened before the review?")); }
    @Test void defaultsToSemantic() { assertEquals(MemoryQueryIntent.SEMANTIC, MemoryQueryIntent.infer("similar study experience")); }
}
