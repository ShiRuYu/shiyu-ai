package com.shiyu.ai.education.implementation.web.request;

import jakarta.validation.constraints.NotBlank;

/** 正式提交的练习答案；网络重试时复用同一个稳定提交键。 */
public record PracticeSubmissionRequest(@NotBlank String answer,
        @NotBlank String submissionKey) {}
