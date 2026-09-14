package com.shiyu.ai.knowledge.implementation.application.document;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/** Markdown 文档解析器 */
@Slf4j
@Component
public class MarkdownDocumentParser implements DocumentParser {

    /**
     * {@code getSupportedFormat} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String getSupportedFormat() {
        return "md";
    }

    /**
     * {@code parse} 执行当前类型定义的业务操作。
     *
     * @param content 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public ParseResult parse(String content) {
        if (content == null || content.isBlank()) {
            return new ParseResult("", "", "");
        }

        String title = "";
        String body = content;
        String metadata = "";

        // 提取第一个 # 标题作为文档标题
        for (String line : content.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("# ") || trimmed.startsWith("#\t")) {
                title = trimmed.replaceFirst("^#\\s+", "").trim();
                break;
            }
        }

        // 提取 front matter（--- 之间的 YAML）
        if (content.startsWith("---")) {
            int end = content.indexOf("---", 3);
            if (end > 0) {
                metadata = content.substring(3, end).trim();
                body = content.substring(end + 3).trim();
            }
        }

        return new ParseResult(title, body, metadata);
    }
}
