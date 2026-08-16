package com.shiyu.ai.education.agent;

import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import com.shiyu.ai.model.chat.ChatMessage;
import com.shiyu.ai.education.domain.model.StudyRecordBO;
import com.shiyu.ai.education.service.AbilityService;
import com.shiyu.ai.education.port.repository.StudyRecordRepository;
import com.shiyu.ai.education.service.AnalyticsService;
import com.shiyu.ai.education.domain.AbilityValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ReportAgent — 学习报告 Agent
 *
 * 职责：根据学习记录和能力数据，生成个性化的学习报告和建议。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportAgent {

    private final ChatEngine chatEngine;
    private final AnalyticsService analyticsService;
    private final StudyRecordRepository studyRecordRepository;
    private final AbilityService abilityService;

    /**
     * 生成学习概览报告
     *
     * @param studentId 学生 ID
     * @return 报告内容
     */
    public String generateOverviewReport(Long studentId) {
        log.info("ReportAgent.generateOverviewReport: studentId={}", studentId);

        // 1. 获取学习记录
        List<StudyRecordBO> records = studyRecordRepository.selectByStudent(studentId);

        // 2. 统计学习数据
        int totalStudySessions = records.size();
        double totalScore = records.stream()
                .filter(r -> r.getScore() != null)
                .mapToDouble(StudyRecordBO::getScore)
                .average()
                .orElse(0.0);
        long totalDuration = records.stream()
                .filter(r -> r.getDurationSec() != null)
                .mapToLong(StudyRecordBO::getDurationSec)
                .sum();

        // 3. 构建 Prompt
        String prompt = buildReportPrompt(studentId, totalStudySessions, totalScore, totalDuration);

        // 4. 调用 LLM 生成报告
        ChatResponse resp = chatEngine.chat(ChatRequest.builder()
                .messages(java.util.List.of(ChatMessage.text("user", prompt)))
                .build());

        if (!resp.isSuccess()) {
            log.error("ReportAgent: LLM 报告生成失败: {}", resp.getErrorMessage());
            return "报告生成失败，请稍后重试。\n统计数据：\n"
                    + "- 学习次数：" + totalStudySessions + "\n"
                    + "- 平均得分：" + String.format("%.1f", totalScore) + "\n"
                    + "- 总学习时长：" + (totalDuration / 60) + " 分钟";
        }

        log.info("ReportAgent.generateOverviewReport: 报告生成完成");
        return resp.getContent();
    }

    private String buildReportPrompt(Long studentId, int totalSessions,
                                      double avgScore, long totalDurationSec) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位学习分析顾问，请根据以下学习数据生成个性化学习报告。\n\n");
        sb.append("## 学习数据\n");
        sb.append("- 学习次数：").append(totalSessions).append(" 次\n");
        sb.append("- 平均得分：").append(String.format("%.1f", avgScore)).append(" 分\n");
        sb.append("- 总学习时长：").append(totalDurationSec / 60).append(" 分钟\n\n");
        sb.append("## 报告要求\n");
        sb.append("1. 总结学生的学习概况和进步情况\n");
        sb.append("2. 指出优势科目和薄弱环节\n");
        sb.append("3. 给出有针对性的学习建议\n");
        sb.append("4. 鼓励学生继续努力\n");
        sb.append("5. 请用中文回答，语气亲切鼓励\n");
        return sb.toString();
    }
}
