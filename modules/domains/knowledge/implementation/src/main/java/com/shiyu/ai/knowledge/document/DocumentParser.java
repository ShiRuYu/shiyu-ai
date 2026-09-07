package com.shiyu.ai.knowledge.document;

import java.nio.charset.StandardCharsets;

/**
 * 文档解析器 SPI
 * 支持多种文档格式解析
 */
public interface DocumentParser {

    String getSupportedFormat();

    ParseResult parse(String content);

    default ParseResult parse(byte[] content) {
        return parse(new String(content, StandardCharsets.UTF_8));
    }

    record ParseResult(String title, String text, String metadata) {}
}
