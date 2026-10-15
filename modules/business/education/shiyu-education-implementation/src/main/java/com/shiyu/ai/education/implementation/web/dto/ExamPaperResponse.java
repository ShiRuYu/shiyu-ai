package com.shiyu.ai.education.implementation.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** 学生可见的试卷，不包含标准答案和解析。 */
public record ExamPaperResponse(
        Long examId,
        Long attemptId,
        String name,
        String subjectCode,
        Integer grade,
        Integer durationMin,
        Integer totalScore,
        Instant deadlineAt,
        List<Section> sections) {

    /** 按顺序排列且不含标准答案的试卷章节。 */
    @Schema(name = "ExamPaperSection")
    public record Section(Long id, String name, Integer orderNo, List<Question> questions) {}

    /** 学生可见且不含评分字段的试题。 */
    @Schema(name = "ExamPaperQuestion")
    public record Question(
            Long id,
            Long questionId,
            String title,
            String type,
            BigDecimal score,
            List<Option> options) {}

    /** 选项值及其展示文本。 */
    @Schema(name = "ExamPaperOption")
    public record Option(String value, String label) {}
}
