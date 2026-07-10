package com.shiyu.ai.knowledge.document;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WordDocumentParser 单元测试
 */
@Tag("dev")
class WordDocumentParserTest {

    private WordDocumentParser parser;

    @BeforeEach
    void setUp() {
        parser = new WordDocumentParser();
    }

    @Test
    void testGetSupportedFormat() {
        assertEquals("docx", parser.getSupportedFormat());
    }

    @Test
    void testParseNonDocxContentAsPlainText() {
        // Non-DOCX content (doesn't start with PK) should be returned as plain text
        String text = "This is plain text, not a .docx file.\nLine 2.";
        DocumentParser.ParseResult result = parser.parse(text);

        assertEquals("", result.title());
        assertEquals(text, result.text());
        assertEquals("", result.metadata());
    }

    @Test
    void testParseNullContent() {
        DocumentParser.ParseResult result = parser.parse((String) null);

        assertEquals("", result.title());
        assertEquals("", result.text());
        assertEquals("", result.metadata());
    }

    @Test
    void testParseEmptyContent() {
        DocumentParser.ParseResult result = parser.parse("");

        assertEquals("", result.title());
        assertEquals("", result.text());
        assertEquals("", result.metadata());
    }

    @Test
    void testParseBlankContent() {
        DocumentParser.ParseResult result = parser.parse("   ");

        assertEquals("", result.title());
        assertEquals("", result.text());
        assertEquals("", result.metadata());
    }

    @Test
    void testParseEmptyByteArray() {
        DocumentParser.ParseResult result = parser.parse(new byte[0]);

        assertEquals("", result.title());
        assertEquals("", result.text());
        assertEquals("", result.metadata());
    }

    @Test
    void testParseNullByteArray() {
        DocumentParser.ParseResult result = parser.parse((byte[]) null);

        assertEquals("", result.title());
        assertEquals("", result.text());
        assertEquals("", result.metadata());
    }
}
