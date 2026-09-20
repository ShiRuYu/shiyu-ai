package com.shiyu.ai.knowledge.implementation.infrastructure.document;

import com.shiyu.ai.knowledge.implementation.application.port.document.DocumentParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

/**
 * 解析或编解码 Html 文档 相关的外部内容和领域数据。
 */
@Component
public class HtmlDocumentParser implements DocumentParser {
    /**
     * 查询 Html 文档 相关业务数据，并返回处理结果。
     *
     * @return 返回 Html 文档 相关操作生成的结果数据。
     */
    @Override
    public String getSupportedFormat() {
        return "html";
    }

    /**
     * 执行 Html 文档 相关业务数据，并返回处理结果。
     *
     * @param content 用于完成本次业务处理的 content 参数。
     * @return 返回 Html 文档 相关操作生成的结果数据。
     */
    @Override
    public ParseResult parse(String content) {
        Document document = Jsoup.parse(content == null ? "" : content);
        return new ParseResult(document.title(), document.text(), "");
    }
}
