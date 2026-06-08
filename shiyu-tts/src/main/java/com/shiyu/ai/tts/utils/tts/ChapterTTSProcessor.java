package com.shiyu.ai.tts.utils.tts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiyu.ai.common.core.utils.UnifiedThreadPoolUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ChapterTTSProcessor
 * 调用开源项目：EasyVoice
 * -------------------
 * 从 input 文件夹读取多个章节文本，
 * 使用 UnifiedThreadPoolUtils 并发调用本地 TTS 接口，
 * 每次最多并发 5 个文件任务，
 * 并将生成的 MP3 下载到 output 文件夹。
 * 支持去除多个指定字符串。
 */
@Slf4j
public class ChapterTTSProcessor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** TTS API 地址 */
    private static final String API_URL = "http://localhost:3000/api/v1/tts/createStream";

    /** 输入与输出目录 */
    private static final Path INPUT_DIR = Paths.get("E:\\output");

    /** 限制最大同时执行任务数 */
    private static final int MAX_CONCURRENT_TASKS = 5;

    /** 去除文本中不需要的多个字符串（可根据需要修改或添加） */
    private static final List<String> REMOVE_STRINGS = List.of();

    public static void main(String[] args) throws Exception {
        if (!Files.exists(INPUT_DIR)) {
            log.error("❌ 输入目录不存在：{}", INPUT_DIR.toAbsolutePath());
            return;
        }

        // 读取所有txt文件
        List<Path> files;
        try (Stream<Path> stream = Files.list(INPUT_DIR)) {
            files = stream.filter(p -> p.toString().endsWith(".txt")).collect(Collectors.toList());
        }

        if (files.isEmpty()) {
            log.warn("⚠️ 未找到任何 .txt 文件");
            return;
        }

        log.info("📝 共检测到文件数：{}", files.size());

        // 限制并发执行数量
        Semaphore semaphore = new Semaphore(MAX_CONCURRENT_TASKS);

        // 使用线程池并发提交
        List<Future<Object>> futures = files.stream()
                .map(file -> UnifiedThreadPoolUtils.submit(() -> {
                    semaphore.acquire(); // 控制并发数
                    try {
                        processFile(file);
                    } finally {
                        semaphore.release();
                    }
                    return null;
                }))
                .toList();

        // 等待所有任务完成
        for (Future<Object> f : futures) {
            try {
                f.get();
            } catch (Exception e) {
                log.error("⚠️ 任务执行出错：{}", e.getMessage());
            }
        }

        log.info("✅ 所有任务执行完毕！");
    }

    /** 处理单个文件 */
    private static void processFile(Path file) {
        String chapterName = file.getFileName().toString().replace(".txt", "");
        try {
            String text = Files.readString(file, StandardCharsets.UTF_8);
            text = removeUnwantedStrings(text);

            log.info("🎙️ 开始生成音频：{}", chapterName);
            callTTSAndSave(chapterName, text);
        } catch (Exception e) {
            log.error("❌ 文件处理失败：{} -> {}", file, e.getMessage(), e);
        }
    }

    /** 去除指定的多个字符串 */
    private static String removeUnwantedStrings(String text) {
        String cleaned = text;
        for (String str : REMOVE_STRINGS) {
            if (str != null && !str.isEmpty()) {
                cleaned = cleaned.replace(str, "");
            }
        }
        return cleaned.trim();
    }

    /** 调用 TTS 接口并保存音频 */
    private static void callTTSAndSave(String fileName, String text) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        String jsonBody = MAPPER.writeValueAsString(
                java.util.Map.of(
                        "text", text,
                        "voice", "zh-CN-YunxiNeural",
                        "rate", "0%",
                        "pitch", "0Hz",
                        "volume", "0%"
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("❌ HTTP错误：{} - {}", response.statusCode(), fileName);
            return;
        }

        log.info("✅ 已生成音频");
    }

}


