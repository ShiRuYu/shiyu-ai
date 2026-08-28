package com.shiyu.ai.knowledge.model;

import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExistingRerankProviderTest {
    private final ChatEngine engine = mock(ChatEngine.class);
    private final ExistingRerankProvider provider = new ExistingRerankProvider(engine);

    @Test
    void returnsDeterministicOrderForSmallCandidateSets() {
        assertEquals("platform", provider.profile());
        assertEquals(List.of(), provider.rerank("q", null, 3));
        assertEquals(List.of(0), provider.rerank("q", List.of("one"), 3));
    }

    @Test
    void parsesValidUniqueIndexesAndClampsTopK() {
        when(engine.chat(any())).thenReturn(ChatResponse.builder().success(true).content("[2], 2, 9, bad, 0").build());
        assertEquals(List.of(2, 0), provider.rerank("q", List.of("a", "b", "c"), 99));
        verify(engine).chat(any());
    }

    @Test
    void fallsBackForEmptyResponseFailureAndProviderException() {
        when(engine.chat(any())).thenReturn(ChatResponse.builder().success(true).content("bad").build());
        assertEquals(List.of(0), provider.rerank("q", List.of("a", "b"), 0));
        when(engine.chat(any())).thenReturn(ChatResponse.builder().success(false).content("1").build());
        assertEquals(List.of(0, 1), provider.rerank("q", List.of("a", "b"), 2));
        when(engine.chat(any())).thenThrow(new IllegalStateException("down"));
        assertEquals(List.of(0), provider.rerank("q", List.of("a", "b"), 1));
        doReturn(null).when(engine).chat(any());
        assertEquals(List.of(0, 1), provider.rerank("q", List.of("a", "b"), 2));
        doReturn(ChatResponse.builder().success(true).content(null).build()).when(engine).chat(any());
        assertEquals(List.of(0), provider.rerank("q", List.of("a", "b"), 1));
    }
}
