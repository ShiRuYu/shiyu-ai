package com.shiyu.ai.knowledge.implementation.application.document;

import org.springframework.stereotype.Component;

/**
 * {@code TextDocumentParser} 承载知识模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Component
public class TextDocumentParser implements DocumentParser {
    /**
     * {@code getSupportedFormat} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String getSupportedFormat() {
        return "txt";
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
