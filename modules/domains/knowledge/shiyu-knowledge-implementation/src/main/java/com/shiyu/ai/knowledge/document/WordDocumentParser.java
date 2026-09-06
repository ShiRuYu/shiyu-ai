package com.shiyu.ai.knowledge.document;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Word 文档解析器 — 基于 Apache POI 5.x
 * 支持提取 .docx 纯文本内容（含标题、段落、表格）
 */
@Slf4j
@Component
public class WordDocumentParser implements DocumentParser {

    @Override
    public String getSupportedFormat() {
        return "docx";
    }

    @Override
    public ParseResult parse(String content) {
        if (content == null || content.isBlank()) {
            return new ParseResult("", "", "");
        }

        // 非真实 .docx 时，作为纯文本返回（测试场景）
        if (!content.startsWith("PK")) {
            return new ParseResult("", content, "");
        }

        byte[] docxBytes;
        try {
            docxBytes = content.getBytes("UTF-8");
        } catch (Exception e) {
            log.warn("DOCX 内容编码转换失败", e);
            return new ParseResult("", content, "");
        }

        return parse(docxBytes);
    }

    /**
     * 解析 .docx 字节数组
     */
    public ParseResult parse(byte[] docxBytes) {
        if (docxBytes == null || docxBytes.length == 0) {
            return new ParseResult("", "", "");
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(docxBytes);
             XWPFDocument document = new XWPFDocument(bais);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {

            String text = extractor.getText();

            // 从文档属性提取标题
            String title = "";
            if (document.getProperties() != null
                    && document.getProperties().getCoreProperties() != null) {
                String coreTitle = document.getProperties().getCoreProperties().getTitle();
                if (coreTitle != null && !coreTitle.isBlank()) {
                    title = coreTitle;
                }
            }

            // 如果文档属性没有标题，取第一段非空行
            if (title.isBlank()) {
                for (String line : text.split("\n")) {
                    String trimmed = line.trim();
                    if (!trimmed.isBlank()) {
                        title = trimmed.length() > 100 ? trimmed.substring(0, 100) : trimmed;
                        break;
                    }
                }
            }

            int paragraphCount = document.getParagraphs().size();
            int tableCount = document.getTables().size();
            String metadata = String.format("paragraphs=%d, tables=%d", paragraphCount, tableCount);

            log.debug("Word 解析完成: paragraphs={}, tables={}, textLength={}",
                    paragraphCount, tableCount, text.length());
            return new ParseResult(title, text, metadata);

        } catch (IOException e) {
            log.error("Word 文档解析失败", e);
            return new ParseResult("", "", "error: " + e.getMessage());
        }
    }
}
