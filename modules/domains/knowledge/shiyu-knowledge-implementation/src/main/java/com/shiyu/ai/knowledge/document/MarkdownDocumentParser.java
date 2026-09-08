package com.shiyu.ai.knowledge.document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Markdown 文档解析器
 */
@Slf4j
@Component
public class MarkdownDocumentParser implements DocumentParser {

    @Override
    public String getSupportedFormat() {
        return "md";
    }

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
