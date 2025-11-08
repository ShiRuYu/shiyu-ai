package com.shiyu.ai.common.core.tts;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class SplitChapters {

    private static final List<String> REMOVE_STRINGS = Arrays.asList(
            "妍希提醒您：由于番茄请求每日三百万+导致服务器压力过大，现在推荐您使用其他大佬的源：",
            "X-大佬的源，支持番茄四合一，支持看所有小说内容，源：https://fanqie.hnxianxin.cn/shuyuan/de9b02bbd567d717c5481121d0edbf30.json"
    );

    public static void main(String[] args) throws IOException {
        String inputPath = "E:\\DownLoad\\玄学丑妃：满朝文武跪求我算卦 作者：东北大米.txt";
        String outputDir = "E:/output/";
        Files.createDirectories(Paths.get(outputDir));

        // 每多少章保存一个文件
        int chaptersPerFile = 100;

        // 提取小说名（去掉路径和后缀）
        String fileName = Paths.get(inputPath).getFileName().toString();
        String novelName = fileName.replaceFirst("\\.txt$", "");

        Pattern chapterPattern = Pattern.compile("第[一二三四五六七八九十百零〇\\d]+章\\s*[^\\r\\n]*");

        List<String> chapterTitles = new ArrayList<>();
        List<String> chapterContents = new ArrayList<>();
        StringBuilder currentContent = new StringBuilder();
        String currentChapterTitle = null;

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputPath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = removeUnwantedStrings(line);

                Matcher matcher = chapterPattern.matcher(line.trim());
                if (matcher.matches()) {
                    if (currentChapterTitle != null) {
                        chapterTitles.add(currentChapterTitle);
                        chapterContents.add(currentContent.toString());
                    }
                    currentChapterTitle = line.trim();
                    currentContent.setLength(0);
                } else {
                    currentContent.append(line).append(System.lineSeparator());
                }
            }
        }

        if (currentChapterTitle != null) {
            chapterTitles.add(currentChapterTitle);
            chapterContents.add(currentContent.toString());
        }

        // 按指定章节数合并保存
        for (int i = 0; i < chapterTitles.size(); i += chaptersPerFile) {
            int end = Math.min(i + chaptersPerFile, chapterTitles.size());
            StringBuilder fileContent = new StringBuilder();
            for (int j = i; j < end; j++) {
                fileContent.append(chapterTitles.get(j)).append(System.lineSeparator());
                fileContent.append(chapterContents.get(j)).append(System.lineSeparator());
            }

            // 构建文件名：小说名 + 起始章-结束章
            String startChapter = chapterTitles.get(i).replaceAll("[^\\d一二三四五六七八九十百零〇]", "");
            String endChapter = chapterTitles.get(end - 1).replaceAll("[^\\d一二三四五六七八九十百零〇]", "");
            String outputFileName = novelName + " " + startChapter + "-" + endChapter + "章.txt";

            Path filePath = Paths.get(outputDir, outputFileName);
            Files.writeString(filePath, fileContent.toString(), StandardCharsets.UTF_8);
            System.out.println("保存：" + filePath.getFileName());
        }

        System.out.println("✅ 分割完成，结果保存在：" + outputDir);
    }

    private static String removeUnwantedStrings(String line) {
        String result = line;
        for (String removeStr : REMOVE_STRINGS) {
            result = result.replace(removeStr, "");
        }
        return result;
    }
}
