package com.shiyu.ai.education.implementation.web.request;

import jakarta.validation.constraints.NotBlank;

/** Formal practice answer with a stable key reused on network retries. */
public record PracticeSubmissionRequest(@NotBlank String answer,
        @NotBlank String submissionKey) {}
