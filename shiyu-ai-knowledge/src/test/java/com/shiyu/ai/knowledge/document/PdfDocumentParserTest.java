package com.shiyu.ai.knowledge.document;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PdfDocumentParser 单元测试
 */
@Tag("dev")
class PdfDocumentParserTest {

    private PdfDocumentParser parser;

    @BeforeEach
    void setUp() {
        parser = new PdfDocumentParser();
    }

    @Test
    void testGetSupportedFormat() {
        assertEquals("pdf", parser.getSupportedFormat());
    }

    @Test
    void testParseNonPdfContentAsPlainText() {
        // Non-PDF content (doesn't start with %PDF) should be returned as plain text
        String text = "This is plain text, not a PDF.\nLine 2.";
        DocumentParser.ParseResult result = parser.parse(text);

        assertEquals("", result.title()); // no PDF header, no title
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
