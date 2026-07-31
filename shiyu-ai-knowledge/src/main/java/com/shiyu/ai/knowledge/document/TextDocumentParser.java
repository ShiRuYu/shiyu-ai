package com.shiyu.ai.knowledge.document;

import org.springframework.stereotype.Component;

@Component
public class TextDocumentParser implements DocumentParser {
    @Override
    public String getSupportedFormat() {
        return "txt";
    }

    @Override
    public ParseResult parse(String content) {
        String text = content == null ? "" : content;
        String title = text.lines().map(String::trim).filter(line -> !line.isEmpty())
                .findFirst().map(line -> line.length() > 100 ? line.substring(0, 100) : line)
                .orElse("");
        return new ParseResult(title, text, "");
    }
}
