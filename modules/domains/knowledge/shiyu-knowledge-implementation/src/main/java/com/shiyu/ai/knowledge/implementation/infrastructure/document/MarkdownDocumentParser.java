package com.shiyu.ai.knowledge.implementation.infrastructure.document;

import com.shiyu.ai.knowledge.implementation.application.port.document.DocumentParser;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * 解析或编解码 Markdown 文档 相关的外部内容和领域数据。
 */
@Slf4j
@Component
public class MarkdownDocumentParser implements DocumentParser {

    /**
     * 查询 Markdown 文档 相关业务数据，并返回处理结果。
     *
     * @return 返回 Markdown 文档 相关操作生成的结果数据。
     */
    @Override
    public String getSupportedFormat() {
        return "md";
    }

    /**
     * 执行 Markdown 文档 相关业务数据，并返回处理结果。
     *
     * @param content 用于完成本次业务处理的 content 参数。
     * @return 返回 Markdown 文档 相关操作生成的结果数据。
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
