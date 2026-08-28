package com.shiyu.ai.knowledge.rag;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChineseChunkSplitterTest {
    private final ChineseChunkSplitter splitter = new ChineseChunkSplitter();

    @Test
    void handlesEmptyInputAndHeadingSections() {
        assertTrue(splitter.split(null).isEmpty());
        assertTrue(splitter.split(" ").isEmpty());
        List<ChunkSplitter.Chunk> chunks = splitter.split("# 第一章\n内容一\n\n## 第二节\n内容二");
        assertEquals(1, chunks.size());
        assertTrue(chunks.getFirst().content().contains("第一章"));
        assertEquals(0, chunks.getFirst().index());
        assertTrue(chunks.getFirst().content().contains("第二节"));
    }

    @Test
    void fallsBackToParagraphsAndKeepsLongContentBounded() {
        List<ChunkSplitter.Chunk> paragraphs = splitter.split("第一段内容\n\n第二段内容");
        assertEquals(1, paragraphs.size());
        assertTrue(paragraphs.getFirst().content().contains("第二段内容"));

        String longText = ("这是用于检索的长段落。 ".repeat(500)) + "\n\n" + ("后续段落。 ".repeat(500));
        List<ChunkSplitter.Chunk> chunks = splitter.split(longText);
        assertTrue(chunks.size() > 1);
        assertEquals(0, chunks.getFirst().index());
        assertEquals(chunks.size() - 1, chunks.getLast().index());
        assertTrue(chunks.stream().allMatch(chunk -> !chunk.content().isBlank()));
    }

    @Test
    void handlesHeadingBoundariesAndMixedTokenEstimation() {
        // Heading-only prefixes are ignored, while numbered and Chinese headings
        // retain their heading text in the following section.
        List<ChunkSplitter.Chunk> headings = splitter.split("前言\n1. 第一节\n正文\n\n第二章\n更多正文");
        assertFalse(headings.isEmpty());
        assertTrue(headings.stream().anyMatch(chunk -> chunk.content().contains("1. 第一节")));
        assertTrue(headings.stream().anyMatch(chunk -> chunk.content().contains("第二章")));

        // Whitespace and ASCII words exercise the token estimator's word-state
        // transitions; a large paragraph forces a chunk rollover and overlap.
        String ascii = ("alpha beta gamma ".repeat(450)) + "\n\n" + ("中文段落。 ".repeat(450));
        List<ChunkSplitter.Chunk> chunks = splitter.split(ascii);
        assertTrue(chunks.size() > 1);
        assertTrue(chunks.stream().anyMatch(chunk -> chunk.startPos() > 0));
    }

    @Test
    void exercisesOverlapAndTokenUtilityBoundaries() throws Exception {
        Method estimate = ChineseChunkSplitter.class.getDeclaredMethod("estimateTokens", String.class);
        estimate.setAccessible(true);
        assertEquals(0, estimate.invoke(splitter, new Object[]{null}));
        assertEquals(0, estimate.invoke(splitter, ""));
        assertEquals(3, estimate.invoke(splitter, "中 a  b"));

        Method overlapChars = ChineseChunkSplitter.class.getDeclaredMethod("getOverlapChars", String.class);
        overlapChars.setAccessible(true);
        assertEquals(0, overlapChars.invoke(splitter, ""));
        assertTrue((Integer) overlapChars.invoke(splitter, "这是一个足够长的中文段落" ) > 0);

        Method overlapText = ChineseChunkSplitter.class.getDeclaredMethod("getOverlapText", List.class, int.class);
        overlapText.setAccessible(true);
        assertEquals("", overlapText.invoke(splitter, List.of("one", "two"), 99));
        assertTrue(((String) overlapText.invoke(splitter, List.of("one", "two"), 1)).contains("one"));
    }
}
