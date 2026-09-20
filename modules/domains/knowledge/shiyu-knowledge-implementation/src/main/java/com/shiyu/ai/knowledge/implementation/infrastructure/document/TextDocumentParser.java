package com.shiyu.ai.knowledge.implementation.infrastructure.document;

import com.shiyu.ai.knowledge.implementation.application.port.document.DocumentParser;

import org.springframework.stereotype.Component;

/**
 * 解析或编解码 Text 文档 相关的外部内容和领域数据。
 */
@Component
public class TextDocumentParser implements DocumentParser {
    /**
     * 查询 Text 文档 相关业务数据，并返回处理结果。
     *
     * @return 返回 Text 文档 相关操作生成的结果数据。
     */
    @Override
    public String getSupportedFormat() {
        return "txt";
    }

    /**
     * 执行 Text 文档 相关业务数据，并返回处理结果。
     *
     * @param content 用于完成本次业务处理的 content 参数。
     * @return 返回 Text 文档 相关操作生成的结果数据。
     */
    @Override
    public ParseResult parse(String content) {
        String text = content == null ? "" : content;
        String title =
                text.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .findFirst()
                        .map(line -> line.length() > 100 ? line.substring(0, 100) : line)
                        .orElse("");
        return new ParseResult(title, text, "");
    }
}
