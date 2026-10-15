package com.shiyu.ai.education.implementation.web.request;

/** 学生辅导请求参数；学生身份始终从登录上下文获取。 */
public record StudentTutorRequest(
        Long knowledgeId, String style, Integer difficulty, Integer count, String targetDate,
        String period) {}
