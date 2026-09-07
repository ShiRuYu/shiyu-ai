package com.shiyu.ai.knowledge.document;

import org.junit.jupiter.api.Test;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentParserTest {

    @Test
    void parsesHtmlMarkdownAndPlainTextTitles() {
        DocumentParser.ParseResult html = new HtmlDocumentParser().parse("<html><title>Title</title><body>Hello <b>world</b></body></html>");
        assertEquals("Title", html.title());
        assertEquals("Title Hello world", html.text());

        DocumentParser.ParseResult markdown = new MarkdownDocumentParser()
                .parse("---\nauthor: me\n---\n# Heading\nBody");
        assertEquals("Heading", markdown.title());
        assertEquals("author: me", markdown.metadata());
        assertTrue(markdown.text().contains("# Heading"));

        DocumentParser.ParseResult text = new TextDocumentParser().parse("\n first line\nsecond");
        assertEquals("first line", text.title());
        assertEquals("\n first line\nsecond", text.text());
    }

    @Test
    void handlesEmptyAndPreExtractedBinaryDocumentInputs() {
        DocumentParser.ParseResult emptyMarkdown = new MarkdownDocumentParser().parse((String) null);
        assertEquals("", emptyMarkdown.text());
        assertEquals("", new MarkdownDocumentParser().parse(" ").title());

        DocumentParser.ParseResult pdf = new PdfDocumentParser().parse("plain text");
        assertEquals("plain text", pdf.text());
        assertEquals("", new PdfDocumentParser().parse(new byte[0]).text());

        DocumentParser.ParseResult word = new WordDocumentParser().parse("plain text");
        assertEquals("plain text", word.text());
        assertEquals("", new WordDocumentParser().parse(new byte[0]).text());
    }

    @Test
    void supportsByteToTextDefaultParserConversion() {
        DocumentParser parser = new TextDocumentParser();
        assertEquals("hello", parser.parse("hello".getBytes(java.nio.charset.StandardCharsets.UTF_8)).text());
        assertEquals("txt", parser.getSupportedFormat());
    }

    @Test
    void parsesRealPdfAndWordBytesAndReportsMalformedInput() throws Exception {
        try (PDDocument pdf = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            pdf.addPage(new PDPage());
            try (PDPageContentStream stream = new PDPageContentStream(pdf, pdf.getPage(0))) {
                stream.beginText(); stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12); stream.newLineAtOffset(50, 700);
                stream.showText("PDF heading"); stream.endText();
            }
            pdf.getDocumentInformation().setTitle("PDF title");
            pdf.save(out);
            DocumentParser.ParseResult parsed = new PdfDocumentParser().parse(out.toByteArray());
            assertEquals("PDF title", parsed.title());
            assertTrue(parsed.text().contains("PDF heading"));
            assertEquals("pages=1", parsed.metadata());
        }
        try (XWPFDocument word = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            word.getProperties().getCoreProperties().setTitle("Word title");
            word.createParagraph().createRun().setText("Word heading");
            word.write(out);
            DocumentParser.ParseResult parsed = new WordDocumentParser().parse(out.toByteArray());
            assertEquals("Word title", parsed.title());
            assertTrue(parsed.text().contains("Word heading"));
            assertTrue(parsed.metadata().startsWith("paragraphs=1"));
        }
        assertTrue(new PdfDocumentParser().parse(new byte[]{1, 2, 3}).metadata().startsWith("error:"));
        assertThrows(RuntimeException.class, () -> new WordDocumentParser().parse(new byte[]{1, 2, 3}));
    }

    @Test
    void derivesTitlesFromFirstContentWhenDocumentMetadataIsAbsent() throws Exception {
        try (PDDocument pdf = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            pdf.addPage(new PDPage());
            try (PDPageContentStream stream = new PDPageContentStream(pdf, pdf.getPage(0))) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText("First PDF paragraph");
                stream.endText();
            }
            pdf.save(out);
            DocumentParser.ParseResult parsed = new PdfDocumentParser().parse(out.toByteArray());
            assertTrue(parsed.text().contains("First PDF paragraph"));
        }
        try (XWPFDocument word = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            word.createParagraph().createRun().setText("First Word paragraph");
            word.write(out);
            DocumentParser.ParseResult parsed = new WordDocumentParser().parse(out.toByteArray());
            assertEquals("First Word paragraph", parsed.title());
        }
        DocumentParser.ParseResult malformedPdf = new PdfDocumentParser().parse("%PDF-invalid");
        assertTrue(malformedPdf.metadata().startsWith("error:"));
    }

    @Test
    void coversParserNullBytesAndMarkdownFrontMatterBoundaries() {
        assertEquals("", new PdfDocumentParser().parse((byte[]) null).text());
        assertEquals("", new WordDocumentParser().parse((byte[]) null).text());

        DocumentParser.ParseResult tabHeading = new MarkdownDocumentParser()
                .parse("#\tTabbed title\nbody");
        assertEquals("Tabbed title", tabHeading.title());

        DocumentParser.ParseResult unterminatedFrontMatter = new MarkdownDocumentParser()
                .parse("---\nauthor: me\n# Body");
        assertEquals("", unterminatedFrontMatter.metadata());
        assertTrue(unterminatedFrontMatter.text().startsWith("---"));

        assertEquals("", new TextDocumentParser().parse((String) null).title());
        String longTitle = "x".repeat(101) + "\nbody";
        assertEquals(100, new TextDocumentParser().parse(longTitle).title().length());
        assertEquals("", new HtmlDocumentParser().parse((String) null).title());
    }
}
