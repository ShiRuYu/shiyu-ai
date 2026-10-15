package com.shiyu.ai.education.implementation.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** 以试卷中原始试题 ID 为标识的学生答案集合。 */
public record SubmitExamRequest(@NotNull List<@Valid Answer> answers) {
    /** 一道试题的 ID 及学生答案。 */
    @Schema(name = "ExamSubmissionAnswer")
    public record Answer(@NotNull Long questionId, String answer) {}
}
