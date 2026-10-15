package com.shiyu.ai.education.implementation.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

/** 考试草稿中按顺序排列的章节及设置分值的试题。 */
public record ExamCompositionRequest(@NotEmpty List<@Valid Section> sections) {
    /** 考试草稿中具有名称的章节。 */
    @Schema(name = "ExamCompositionSection")
    public record Section(@NotBlank String name, @NotEmpty List<@Valid Question> questions) {}
    /** 保密试题及其分配的分值。 */
    @Schema(name = "ExamCompositionQuestion")
    public record Question(@NotNull Long questionId, @NotNull @Positive BigDecimal score) {}
}
