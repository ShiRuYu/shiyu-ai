package com.shiyu.ai.knowledge.implementation.infrastructure.document;

import com.shiyu.ai.knowledge.implementation.application.port.document.DocumentParser;

import lombok.extern.slf4j.Slf4j;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 解析或编解码 Pdf 文档 相关的外部内容和领域数据。
 */
@Slf4j
@Component
public class PdfDocumentParser implements DocumentParser {

    /**
     * 查询 Pdf 文档 相关业务数据，并返回处理结果。
     *
     * @return 返回 Pdf 文档 相关操作生成的结果数据。
     */
    @Override
    public String getSupportedFormat() {
        return "pdf";
    }

    /**
     * 执行 Pdf 文档 相关业务数据，并返回处理结果。
     *
     * @param content 用于完成本次业务处理的 content 参数。
     * @return 返回 Pdf 文档 相关操作生成的结果数据。
     */
    @Override
    public ParseResult parse(String content) {
        if (content == null || content.isBlank()) {
            return new ParseResult("", "", "");
        }

        if (!content.startsWith("%PDF")) {
            return new ParseResult("", content, "");
        }

        byte[] pdfBytes;
        try {
            pdfBytes = content.getBytes("UTF-8");
        } catch (Exception e) {
            log.warn("PDF 内容编码转换失败", e);
            return new ParseResult("", content, "");
        }

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            // 尝试从文档信息中提取标题
            String title = "";
            if (document.getDocumentInformation() != null) {
                String docTitle = document.getDocumentInformation().getTitle();
                if (docTitle != null && !docTitle.isBlank()) {
                    title = docTitle;
                }
            }

            // 如果文档信息没有标题，取第一段非空行
            if (title.isBlank()) {
                for (String line : text.split("\n")) {
                    String trimmed = line.trim();
                    if (!trimmed.isBlank()) {
                        title = trimmed.length() > 100 ? trimmed.substring(0, 100) : trimmed;
                        break;
                    }
                }
            }

            int pageCount = document.getNumberOfPages();
            String metadata = String.format("pages=%d", pageCount);

            log.debug("PDF 解析完成: pages={}, textLength={}", pageCount, text.length());
            return new ParseResult(title, text, metadata);

        } catch (IOException e) {
            log.error("PDF 解析失败", e);
            return new ParseResult("", "", "error: 文档解析失败");
        }
    }

    /** 解析 PDF 字节数组 */
    public ParseResult parse(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            return new ParseResult("", "", "");
        }

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            String title = "";
            if (document.getDocumentInformation() != null) {
                String docTitle = document.getDocumentInformation().getTitle();
                if (docTitle != null && !docTitle.isBlank()) {
                    title = docTitle;
                }
            }

            int pageCount = document.getNumberOfPages();
            String metadata = String.format("pages=%d", pageCount);

            return new ParseResult(title, text, metadata);

        } catch (IOException e) {
            log.error("PDF 解析失败", e);
            return new ParseResult("", "", "error: 文档解析失败");
        }
    }
}
