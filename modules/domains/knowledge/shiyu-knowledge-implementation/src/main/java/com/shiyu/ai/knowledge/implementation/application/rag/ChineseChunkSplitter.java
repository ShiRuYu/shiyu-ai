package com.shiyu.ai.knowledge.implementation.application.rag;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code ChineseChunkSplitter} 承载知识模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Slf4j
public class ChineseChunkSplitter implements ChunkSplitter {

    /**
     * MAX_TOKENS 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final int MAX_TOKENS = 800;
    /**
     * MIN_TOKENS 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final int MIN_TOKENS = 300;
    /**
     * OVERLAP_TOKENS 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final int OVERLAP_TOKENS = 50;

    private static final Pattern HEADING_PATTERN =
            Pattern.compile(
                    "^(#{1,6}\\s+|第[一二三四五六七八九十百千]+[章节篇部]|\\d+\\.\\s+|[一二三四五六七八九十]+[、\\.])",
                    Pattern.MULTILINE);

    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("\\n\\s*\\n|\\r\\n\\s*\\r\\n");

    /**
     * {@code split} 执行当前类型定义的业务操作。
     *
     * @param text 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Chunk> split(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<String> paragraphs = splitByHeadings(text);
        if (paragraphs.size() <= 1) {
            paragraphs = splitByParagraphs(text);
        }

        return mergeChunks(paragraphs);
    }

    private List<String> splitByHeadings(String text) {
        List<String> sections = new ArrayList<>();
        Matcher matcher = HEADING_PATTERN.matcher(text);
        int lastEnd = 0;
        String lastHeading = "";

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String body = text.substring(lastEnd, matcher.start()).trim();
                if (!body.isEmpty()) {
                    sections.add(lastHeading + body);
                }
            }
            lastHeading = matcher.group().trim() + "\n";
            lastEnd = matcher.start();
        }

        if (lastEnd < text.length()) {
            String body = text.substring(lastEnd).trim();
            if (!body.isEmpty()) {
                sections.add(lastHeading + body);
            }
        }

        return sections;
    }

    private List<String> splitByParagraphs(String text) {
        List<String> paragraphs = new ArrayList<>();
        String[] parts = PARAGRAPH_PATTERN.split(text);
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                paragraphs.add(trimmed);
            }
        }
        if (paragraphs.isEmpty()) {
            paragraphs.add(text.trim());
        }
        return paragraphs;
    }

    private List<Chunk> mergeChunks(List<String> paragraphs) {
        List<Chunk> chunks = new ArrayList<>();
        int pos = 0;
        int index = 0;
        StringBuilder current = new StringBuilder();
        int chunkStart = 0;

        for (String para : paragraphs) {
            int paraTokens = estimateTokens(para);

            if (current.length() > 0
                    && estimateTokens(current.toString()) + paraTokens > MAX_TOKENS) {
                if (estimateTokens(current.toString()) >= MIN_TOKENS || chunks.isEmpty()) {
                    chunks.add(new Chunk(current.toString().trim(), index++, chunkStart, pos));
                    chunkStart = Math.max(0, pos - getOverlapChars(current.toString()));
                    current = new StringBuilder();
                    if (chunkStart > 0) {
                        String overlap = getOverlapText(paragraphs, chunkStart);
                        current.append(overlap);
                    }
                }
            }

            if (current.length() == 0) {
                chunkStart = pos;
            }
            current.append(para).append("\n");
            pos += para.length() + 1;
        }

        if (!current.isEmpty()) {
            chunks.add(new Chunk(current.toString().trim(), index, chunkStart, pos));
        }

        log.debug("ChunkSplitter: {} chars → {} chunks", pos, chunks.size());
        return chunks;
    }

    private int getOverlapChars(String text) {
        int totalTokens = estimateTokens(text);
        if (totalTokens <= 0) return 0;
        int charsPerToken = text.length() / totalTokens;
        return Math.min(OVERLAP_TOKENS * charsPerToken, text.length() / 2);
    }

    private String getOverlapText(List<String> paragraphs, int startPos) {
        int pos = 0;
        StringBuilder overlap = new StringBuilder();
        for (String p : paragraphs) {
            if (pos + p.length() > startPos) {
                overlap.append(p).append("\n");
            }
            pos += p.length() + 1;
        }
        return overlap.toString();
    }

    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        int chineseChars = 0;
        int asciiWords = 0;
        boolean inWord = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 0x4E00 && c <= 0x9FFF) {
                chineseChars++;
                inWord = false;
            } else if (Character.isWhitespace(c)) {
                inWord = false;
            } else {
                if (!inWord) {
                    asciiWords++;
                    inWord = true;
                }
            }
        }

        return chineseChars + asciiWords;
    }
}
