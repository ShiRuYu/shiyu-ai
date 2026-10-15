package com.shiyu.ai.education.implementation.web.request;

import jakarta.validation.constraints.NotBlank;

/** 对普通公开练习题提交的选项。 */
public record PracticeCheckRequest(@NotBlank String answer) {}
