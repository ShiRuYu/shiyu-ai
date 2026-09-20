package com.shiyu.ai.knowledge.implementation.infrastructure.document;

import com.shiyu.ai.knowledge.implementation.application.port.document.DocumentParser;

import lombok.extern.slf4j.Slf4j;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 解析或编解码 Word 文档 相关的外部内容和领域数据。
 */
@Slf4j
@Component
public class WordDocumentParser implements DocumentParser {

    /**
     * 查询 Word 文档 相关业务数据，并返回处理结果。
     *
     * @return 返回 Word 文档 相关操作生成的结果数据。
     */
    @Override
    public String getSupportedFormat() {
        return "docx";
    }

    /**
     * 执行 Word 文档 相关业务数据，并返回处理结果。
     *
     * @param content 用于完成本次业务处理的 content 参数。
     * @return 返回 Word 文档 相关操作生成的结果数据。
     */
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

    /** 解析 .docx 字节数组 */
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

            log.debug(
                    "Word 解析完成: paragraphs={}, tables={}, textLength={}",
                    paragraphCount,
                    tableCount,
                    text.length());
            return new ParseResult(title, text, metadata);

        } catch (IOException e) {
            log.error("Word 文档解析失败", e);
            return new ParseResult("", "", "error: 文档解析失败");
        }
    }
}
