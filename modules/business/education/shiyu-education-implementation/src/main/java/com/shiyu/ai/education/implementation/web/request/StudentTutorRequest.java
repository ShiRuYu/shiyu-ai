package com.shiyu.ai.education.implementation.web.request;

/** Student tutor inputs; student identity is always taken from the login context. */
public record StudentTutorRequest(
        Long knowledgeId, String style, Integer difficulty, Integer count, String targetDate,
        String period) {}
