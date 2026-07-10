package com.shiyu.ai.knowledge.document;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MarkdownDocumentParser 单元测试
 */
@Tag("dev")
class MarkdownDocumentParserTest {

    private MarkdownDocumentParser parser;

    @BeforeEach
    void setUp() {
        parser = new MarkdownDocumentParser();
    }

    @Test
    void testGetSupportedFormat() {
        assertEquals("markdown", parser.getSupportedFormat());
    }

    @Test
    void testParseWithTitle() {
        String md = "# Hello World\n\nThis is a paragraph.\n\n## Section 1\nContent here.";
        DocumentParser.ParseResult result = parser.parse(md);

        assertEquals("Hello World", result.title());
        assertTrue(result.text().contains("This is a paragraph."));
        assertTrue(result.text().contains("## Section 1"));
    }

    @Test
    void testParseWithoutTitle() {
        String md = "Just a plain paragraph.\n\nAnother paragraph.";
        DocumentParser.ParseResult result = parser.parse(md);

        assertEquals("", result.title());
        assertEquals(md, result.text());
    }

    @Test
    void testParseWithFrontMatter() {
        String md = "---\ntitle: My Doc\ndate: 2024-01-01\n---\n\n# Content Title\n\nBody text.";
        DocumentParser.ParseResult result = parser.parse(md);

        assertEquals("Content Title", result.title());
        assertTrue(result.metadata().contains("title: My Doc"));
        assertTrue(result.metadata().contains("date: 2024-01-01"));
        assertTrue(result.text().contains("Body text."));
    }

    @Test
    void testParseEmptyContent() {
        DocumentParser.ParseResult result = parser.parse("");

        assertEquals("", result.title());
        assertEquals("", result.text());
        assertEquals("", result.metadata());
    }

    @Test
    void testParseNullContent() {
        DocumentParser.ParseResult result = parser.parse(null);

        assertEquals("", result.title());
        assertEquals("", result.text());
        assertEquals("", result.metadata());
    }

    @Test
    void testParseBlankContent() {
        DocumentParser.ParseResult result = parser.parse("   \n  \n  ");

        assertEquals("", result.title());
        assertEquals("", result.text());
        assertEquals("", result.metadata());
    }

    @Test
    void testLongTitleTruncatedInLogicalExtraction() {
        // Title extraction logic only takes first # heading
        String md = "# First Title\n\nSome text.\n\n# Second Title";
        DocumentParser.ParseResult result = parser.parse(md);

        assertEquals("First Title", result.title());
    }
}
