package com.shiyu.ai.knowledge.document;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class HtmlDocumentParser implements DocumentParser {
    @Override
    public String getSupportedFormat() {
        return "html";
    }

    @Override
    public ParseResult parse(String content) {
        Document document = Jsoup.parse(content == null ? "" : content);
        return new ParseResult(document.title(), document.text(), "");
    }
}
