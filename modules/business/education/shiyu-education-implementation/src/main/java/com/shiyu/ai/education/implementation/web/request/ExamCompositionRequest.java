package com.shiyu.ai.education.implementation.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

/** Ordered sections and scored questions of an exam draft. */
public record ExamCompositionRequest(@NotEmpty List<@Valid Section> sections) {
    /** Named section in a draft exam. */
    @Schema(name = "ExamCompositionSection")
    public record Section(@NotBlank String name, @NotEmpty List<@Valid Question> questions) {}
    /** Confidential question and its assigned score. */
    @Schema(name = "ExamCompositionQuestion")
    public record Question(@NotNull Long questionId, @NotNull @Positive BigDecimal score) {}
}
