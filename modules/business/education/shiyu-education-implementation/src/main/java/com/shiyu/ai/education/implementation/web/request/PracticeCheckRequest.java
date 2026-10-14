package com.shiyu.ai.education.implementation.web.request;

import jakarta.validation.constraints.NotBlank;

/** A submitted choice for an ordinary public practice question. */
public record PracticeCheckRequest(@NotBlank String answer) {}
