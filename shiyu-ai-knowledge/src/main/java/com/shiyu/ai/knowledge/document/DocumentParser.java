package com.shiyu.ai.knowledge.document;

/**
 * 文档解析器 SPI
 * 支持多种文档格式解析
 */
public interface DocumentParser {

    String getSupportedFormat();

    ParseResult parse(String content);

    record ParseResult(String title, String text, String metadata) {}
}
