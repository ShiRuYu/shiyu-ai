package com.shiyu.ai.education.implementation.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Student answers keyed by the original question ID in the paper. */
public record SubmitExamRequest(@NotNull List<@Valid Answer> answers) {
    /** One question ID and the student's answer. */
    @Schema(name = "ExamSubmissionAnswer")
    public record Answer(@NotNull Long questionId, String answer) {}
}
